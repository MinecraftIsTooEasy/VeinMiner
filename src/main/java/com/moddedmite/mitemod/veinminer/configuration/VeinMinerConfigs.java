package com.moddedmite.mitemod.veinminer.configuration;

import fi.dy.masa.malilib.config.ConfigFactory;
import fi.dy.masa.malilib.config.SimpleConfigs;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigEnum;
import fi.dy.masa.malilib.config.options.ConfigInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * ManyLib-backed configuration for VeinMiner.
 * Config file: config/veinminer.json
 * <p>
 * Config options are static so they exist before the super() call
 * in the constructor (SimpleConfigs needs the values list in its
 * constructor, and its {@code values} field is final).
 * {@link ConfigurationValues} reads from and writes to these config objects.
 */
public class VeinMinerConfigs extends SimpleConfigs {

    public static VeinMinerConfigs INSTANCE;

    // --- Value config options (static so available for super() call) ---
    public static final ConfigInteger blockLimit = ConfigFactory.ofInteger(
            "veinminer.limit.blocks", 800, -1, Integer.MAX_VALUE);
    public static final ConfigInteger radiusLimit = ConfigFactory.ofInteger(
            "veinminer.limit.radius", 20, -1, 1000);
    public static final ConfigInteger blocksPerTick = ConfigFactory.ofInteger(
            "veinminer.limit.blocksPerTick", 10, 1, 100);
    public static final ConfigInteger hungerMultiplier = ConfigFactory.ofInteger(
            "veinminer.hungermodifier", 100, 0, 100);
    public static final ConfigInteger experienceMultiplier = ConfigFactory.ofInteger(
            "veinminer.expmodifier", 0, 0, Integer.MAX_VALUE);
    public static final ConfigBoolean enableAllBlocks = ConfigFactory.ofBoolean(
            "veinminer.override.allBlocks");
    public static final ConfigBoolean enableAllTools = ConfigFactory.ofBoolean(
            "veinminer.override.allTools");
    public static final ConfigEnum<PreferredModeEnum> preferredMode = ConfigFactory.ofEnum(
            "veinminer.client.preferredMode", PreferredModeEnum.PRESSED);
    public static final ConfigBoolean showLoginMessage = ConfigFactory.ofBoolean(
            "veinminer.client.showLoginMessage", "true");

    private static final List<ConfigBase<?>> VALUES = buildValues();

    private static List<ConfigBase<?>> buildValues() {
        List<ConfigBase<?>> list = new ArrayList<>();
        list.add(blockLimit);
        list.add(radiusLimit);
        list.add(blocksPerTick);
        list.add(hungerMultiplier);
        list.add(experienceMultiplier);
        list.add(enableAllBlocks);
        list.add(enableAllTools);
        list.add(preferredMode);
        list.add(showLoginMessage);
        return list;
    }

    public VeinMinerConfigs() {
        super("veinminer", null, VALUES, "VeinMiner configuration");
        INSTANCE = this;
    }
}
