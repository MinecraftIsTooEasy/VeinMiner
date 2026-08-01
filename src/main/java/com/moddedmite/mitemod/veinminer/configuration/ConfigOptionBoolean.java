package com.moddedmite.mitemod.veinminer.configuration;

public class ConfigOptionBoolean {
    public boolean value;
    public final boolean valueDefault;
    public final String configName;
    public final String description;

    public ConfigOptionBoolean(boolean valueDefault, String configName, String description) {
        this.valueDefault = valueDefault;
        this.configName = configName;
        this.description = description;
    }
}
