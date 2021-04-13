package com.mammb.javaee8.starter.dev;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DevService {

    private static final Logger log = LoggerFactory.getLogger(DevService.class);

    private static final boolean DEV_ENABLED = System.getProperty("env", "").equals("dev");
    public static final String EMBED_ROOT = DEV_ENABLED ? "build/tmp/embedded" : "./";

    private final GlassFish glassFish;
    private final ExecutorService executor;
    private final List<Runnable> closer;
    private final int port;
    private final String moduleName;
    private final ArchiveType archiveType;


    public DevService(GlassFish glassFish, int port, String moduleName, ArchiveType archiveType) {
        this.glassFish = glassFish;
        this.port = port;
        this.moduleName = moduleName;
        this.archiveType = archiveType;
        this.executor = Executors.newFixedThreadPool(2);
        this.closer = new ArrayList<>();
        this.closer.add(executor::shutdown);
    }


    public void start() {

        if (!DEV_ENABLED) {
            log.warn("Dev mode is disabled.");
            return;
        }

        final String path = String.format("../%s", moduleName);

        executor.submit(() -> {
            Path source = Paths.get(path + "/src/main/webapp/");
            log.info("Start watch service. watch path[{}]", source.toString());
            FsWatcher fsWatcher = FsWatcher.of(source).listenBy(webappSync(source, moduleName)).sensitive();
            closer.add(fsWatcher::close);
            fsWatcher.watch();
        });

        executor.submit(() -> {
            Path source = Paths.get(path + "/build/libs/");
            log.info("Start watch service. watch path[{}]", source.toString());
            FsWatcher archiveWatcher = FsWatcher.of(source).listenBy(archiveSync());
            closer.add(archiveWatcher::close);
            archiveWatcher.watch();
        });
    }


    public void browse() throws Exception {
        if (!DEV_ENABLED) {
            return;
        }
        System.getProperties().put("java.awt.headless", "false");
        Desktop.getDesktop().browse(
                new URI(String.format("http://localhost:%s/%s/index.html", port, moduleName))
        );
        System.getProperties().put("java.awt.headless", "true");
    }


    public void close() {
        closer.forEach(Runnable::run);
    }


    private Consumer<Set<FsEvent>> webappSync(final Path source, final String module) {

        return events -> {
            try {
                for (FsEvent event : events) {
                    if (event.isDeleted()) {
                        continue; // ignore
                    }

                    if (!(event.stringPath().endsWith(".xhtml") ||
                            event.stringPath().endsWith(".html") ||
                            event.stringPath().endsWith(".css") ||
                            event.stringPath().endsWith(".js"))) {
                        // only web resources
                        continue;
                    }

                    String editedPath = event.stringPath().replace(source.toString() + "/", "");
                    String modulePath = archiveType.isEar()
                            ? String.format("applications/ear/%s_war/", module)
                            : String.format("applications/%s/", module);

                    Path destination = gfRoot().resolve(modulePath).resolve(editedPath);
                    Files.copy(event.path(), destination, StandardCopyOption.REPLACE_EXISTING);
                    log.info("apply resource change.[{}]", destination);
                }
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        };
    }

    private Consumer<Set<FsEvent>> archiveSync() {
        return events -> {
            try {
                for (FsEvent event : events) {
                    if (!event.isModified() ||
                        !event.stringPath().endsWith(archiveType.getExtension())) {
                        continue;
                    }
                    log.info("Detect archive changed.[{}]", event.path().toString());
                    Deployer deployer = glassFish.getDeployer();
                    deployer.undeploy(moduleName, "--cascade=true");
                    log.info("Complete undeploy");

                    String appName = deployer.deploy(event.path().toFile(), "--force", "true");
                    log.info("Complete deploy.[{}]", appName);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Path gfRoot() {
        final File[] files = Path.of(EMBED_ROOT).toFile().listFiles();
        return Arrays.stream(Objects.requireNonNull(files))
                .filter(File::isDirectory)
                .filter(file -> file.getName().startsWith("gfembed"))
                .sorted(Comparator.comparingLong(File::lastModified))
                .map(File::toPath)
                .findFirst()
                .orElseThrow();
    }

}
