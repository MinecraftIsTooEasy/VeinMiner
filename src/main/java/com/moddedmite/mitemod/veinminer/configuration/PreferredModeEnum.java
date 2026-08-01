package com.moddedmite.mitemod.veinminer.configuration;

import com.moddedmite.mitemod.veinminer.util.PreferredMode;

/**
 * Enum counterpart of {@link PreferredMode} int constants, for ManyLib's ConfigEnum.
 * Ordinals match the int constants (DISABLED=0, PRESSED=1, ...).
 */
public enum PreferredModeEnum {
    DISABLED,
    PRESSED,
    RELEASED,
    SNEAK_ACTIVE,
    SNEAK_INACTIVE;

    public int toInt() {
        return ordinal();
    }

    public static PreferredModeEnum fromInt(int mode) {
        PreferredModeEnum[] values = values();
        if (mode < 0 || mode >= values.length) {
            return PRESSED;
        }
        return values[mode];
    }

    public String toConfigString() {
        switch (this) {
            case DISABLED: return "disabled";
            case PRESSED: return "pressed";
            case RELEASED: return "released";
            case SNEAK_ACTIVE: return "sneak";
            case SNEAK_INACTIVE: return "no_sneak";
        }
        return "pressed";
    }

    public static PreferredModeEnum fromConfigString(String s) {
        switch (s) {
            case "disabled": return DISABLED;
            case "pressed": return PRESSED;
            case "released": return RELEASED;
            case "sneak": return SNEAK_ACTIVE;
            case "no_sneak": return SNEAK_INACTIVE;
        }
        return PRESSED;
    }
}
