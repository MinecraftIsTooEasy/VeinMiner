package com.moddedmite.mitemod.veinminer.configuration;

public class ConfigToolValue {
    ConfigOptionBoolean autodetectToggle;
    ConfigOptionString autodetectList;
    ConfigOptionString blockIdList;
    ConfigOptionString toolIdList;

    public ConfigToolValue(String toolName, boolean autodetectDefault, String autodetectStringDefault, String blockListDefault, String toolListDefault) {
        autodetectToggle = new ConfigOptionBoolean(autodetectDefault, String.format("autodetect.blocks.%s.enable", toolName),
                String.format("Autodetect blocks for the %s list. [default: %s]", toolName, autodetectDefault ? "true" : "false"));
        autodetectList = new ConfigOptionString(autodetectStringDefault, String.format("autodetect.blocks.%s.prefixes", toolName),
                String.format("List of prefixes to autodetect as blocks to be used with a %s. [default: '%s']", toolName, autodetectStringDefault));
        blockIdList = new ConfigOptionString(blockListDefault, String.format("blockList.%s", toolName),
                String.format("Block names to auto-mine when using a configured %s. [default: '%s']", toolName, blockListDefault));
        toolIdList = new ConfigOptionString(toolListDefault, String.format("itemList.%s", toolName),
                String.format("Item names to use as a %s. [default '%s']", toolName, toolListDefault));
    }
}
