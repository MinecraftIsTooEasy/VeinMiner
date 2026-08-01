package com.moddedmite.mitemod.veinminer.api;

public enum Permission {
    FORCE_ALLOW,
    ALLOW,
    DENY,
    FORCE_DENY;

    public boolean isAllowed() {
        return this == ALLOW || this == FORCE_ALLOW;
    }

    public boolean isDenied() {
        return this == DENY || this == FORCE_DENY;
    }
}
