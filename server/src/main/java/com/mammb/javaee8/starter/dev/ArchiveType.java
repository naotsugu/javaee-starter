package com.mammb.javaee8.starter.dev;

public enum ArchiveType {

    WAR,
    EAR,
    ;

    public boolean isEar() {
        return this == EAR;
    }

    public boolean isWar() {
        return this == WAR;
    }

    public String getExtension() {
        return "." + this.name().toLowerCase();
    }
}
