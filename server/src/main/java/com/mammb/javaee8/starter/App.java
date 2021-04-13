package com.mammb.javaee8.starter;

import com.mammb.javaee8.starter.dev.ArchiveType;
import com.mammb.javaee8.starter.dev.DevService;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final int port = 8083;
    private static final ArchiveType archiveType = ArchiveType.WAR;

    static {
        System.setProperty("logbackDisableServletContainerInitializer", "true");
        System.setProperty("java.util.logging.config.file",
                App.class.getResource("/logging.properties").getPath());
    }

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    public void start() throws Exception {

        final String embeddedRoot = DevService.EMBED_ROOT;

        BootstrapProperties bootstrap = new BootstrapProperties();
        GlassFishRuntime runtime = GlassFishRuntime.bootstrap(bootstrap);
        GlassFishProperties properties = new GlassFishProperties();
        properties.setPort("http-listener", port);
        //properties.setPort("https-listener", 8184);
        properties.setProperty("glassfish.embedded.tmpdir", embeddedRoot);
        GlassFish glassfish = runtime.newGlassFish(properties);

        glassfish.start();
        Deployer deployer = glassfish.getDeployer();
        String appName = deployer.deploy(getArchive(), "--force", "true");
        log.info("Complete deploy.[{}]", appName);

        DevService devService = new DevService(glassfish, port, "web", archiveType);
        devService.start();
        devService.browse();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                devService.close();
                glassfish.dispose();
                deleteRecursively(Path.of(embeddedRoot));
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }));
    }

    private static File getArchive() {
        return Arrays.stream(System.getProperty("java.class.path").split(":"))
                .filter(s -> s.endsWith(archiveType.getExtension()))
                .map(File::new)
                .findFirst()
                .orElseThrow();
    }

    private static void deleteRecursively(Path pathToBeDeleted) throws IOException {
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
