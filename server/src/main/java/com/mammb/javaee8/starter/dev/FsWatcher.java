package com.mammb.javaee8.starter.dev;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class FsWatcher {

    private static final Logger log = LoggerFactory.getLogger(FsWatcher.class);

    private final WatchService watchService;
    private final Map<WatchKey, Path> watchKeys;
    private final Path watchRoot;
    private WatchEvent.Modifier modifier;
    private Consumer<Set<FsEvent>> listener;


    private FsWatcher(Path watchRoot) {
        try {
            this.watchRoot = Objects.requireNonNull(watchRoot);
            this.watchService = FileSystems.getDefault().newWatchService();
            this.watchKeys = new HashMap<>();
            this.modifier = SensitivityWatchEventModifier.MEDIUM;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FsWatcher of(Path watchRoot) {
        return new FsWatcher(watchRoot);
    }

    public FsWatcher listenBy(Consumer<Set<FsEvent>> listener) {
        this.listener = listener;
        return this;
    }

    public FsWatcher sensitive() {
        this.modifier = SensitivityWatchEventModifier.HIGH;
        return this;
    }

    public void watch() {
        try {
            registerRecursive(watchRoot);

            while (true) {
                WatchKey watchKey = watchService.take();
                final Path dir = watchKeys.get(watchKey);
                if (dir == null) {
                    continue;
                }

                Set<FsEvent> paths = new TreeSet<>();
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind() == OVERFLOW) {
                        continue;
                    }
                    WatchEvent.Kind<?> kind = event.kind();
                    Object context = event.context();
                    if (context instanceof Path) {
                        final Path absPath = dir.resolve((Path) context);
                        if (Files.isDirectory(absPath)) {
                            if (kind == ENTRY_CREATE) {
                                registerRecursive(absPath);
                            } else if (kind == ENTRY_DELETE) {
                                watchKeys.remove(watchKey);
                            }
                        } else {
                            paths.add(FsEvent.of(kind, absPath));
                        }
                    }
                }
                if (watchKeys.containsKey(watchKey)) {
                    watchKey.reset();
                }

                if (!paths.isEmpty() && Objects.nonNull(listener)) {
                    listener.accept(paths);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.warn("watchService interrupted.");
        }
    }

    private void registerRecursive(final Path root) throws IOException {
        if (!root.toFile().exists() || !root.toFile().isDirectory()) {
            throw new RuntimeException("[" + root + "] does not exist or is not a directory");
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                if (!watchKeys.containsValue(dir)) {
                    WatchKey watchKey = dir.register(watchService,
                            new WatchEvent.Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY },
                            modifier);
                    watchKeys.put(watchKey, dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void close() {
        try {
            watchService.close();
        } catch (IOException ignore) {

        }
    }

}
