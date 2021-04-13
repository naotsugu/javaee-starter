package com.mammb.javaee8.starter.dev;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FsEvent implements Comparable<FsEvent> {

    private enum Type { CREATED, MODIFIED, DELETED }
    private final Path path;
    private final Type type;

    public FsEvent(Path path, Type type) {
        this.path = Objects.requireNonNull(path);
        this.type = Objects.requireNonNull(type);
    }

    public static FsEvent of(WatchEvent.Kind<?> kind, Path path) {
        if (kind == ENTRY_CREATE) {
            return new FsEvent(path, Type.CREATED);
        } else if (kind == ENTRY_MODIFY) {
            return new FsEvent(path, Type.MODIFIED);
        } else if (kind == ENTRY_DELETE) {
            return new FsEvent(path, Type.DELETED);
        }
        throw new RuntimeException("Unsupported Kind." + kind);
    }

    public Path path() {
        return path;
    }
    public String stringPath() {
        return path.toString();
    }
    public boolean isCreated() {
        return type == Type.CREATED;
    }
    public boolean isModified() {
        return type == Type.MODIFIED;
    }
    public boolean isDeleted() {
        return type == Type.DELETED;
    }

    @Override
    public int compareTo(FsEvent other) {
        return path.compareTo(other.path);
    }

}

