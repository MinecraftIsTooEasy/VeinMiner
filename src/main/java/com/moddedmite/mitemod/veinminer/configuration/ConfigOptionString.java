package com.moddedmite.mitemod.veinminer.configuration;

public class ConfigOptionString {
    public String value;
    public final String valueDefault;
    public final String configName;
    public final String description;

    public ConfigOptionString(String valueDefault, String configName, String description) {
        this.valueDefault = valueDefault;
        this.configName = configName;
        this.description = description;
    }
}
