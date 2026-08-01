package com.moddedmite.mitemod.veinminer.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.lib.MinerLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Stores raw config values. Main config options are backed by ManyLib's
 * {@link VeinMinerConfigs} (config/veinminer.json). The Properties file
 * (config/veinminer/general.cfg) is retained only for equalBlocks and
 * per-tool autodetect settings which don't fit ManyLib's config option types.
 */
public class ConfigurationValues {

    private File configFileProps;
    private File configFileJson;
    private Properties props;

    public HashMap<ToolType, ConfigToolValue> toolConfig = new HashMap<ToolType, ConfigToolValue>(ToolType.values().length);

    public String BLOCK_EQUIVALENCY_LIST;
    public static final String BLOCK_EQUIVALENCY_LIST_DEFAULT = "";

    public JsonElement toolsAndBlocks;
    public Map<String, Tool> defaultTools;

    public ConfigurationValues(File defaultConfig, File toolsJson) {
        configFileProps = defaultConfig;
        configFileJson = toolsJson;
        props = new Properties();

        defaultTools = new HashMap<String, Tool>();
        defaultTools.put("axe", new Tool("Axe", "", new String[]{}, new String[]{}));
        defaultTools.put("hoe", new Tool("Hoe", "", new String[]{}, new String[]{}));
        defaultTools.put("pickaxe", new Tool("Pickaxe", "", new String[]{}, new String[]{}));
        defaultTools.put("shears", new Tool("Shears", "", new String[]{}, new String[]{}));
        defaultTools.put("shovel", new Tool("Shovel", "", new String[]{}, new String[]{}));

        toolConfig.put(ToolType.AXE, new ConfigToolValue("axe", false, "", "", ""));
        toolConfig.put(ToolType.HOE, new ConfigToolValue("hoe", false, "", "", ""));
        toolConfig.put(ToolType.PICKAXE, new ConfigToolValue("pickaxe", false, "", "", ""));
        toolConfig.put(ToolType.SHEARS, new ConfigToolValue("shears", false, "", "", ""));
        toolConfig.put(ToolType.SHOVEL, new ConfigToolValue("shovel", false, "", "", ""));

        toolsAndBlocks = new JsonObject();

        loadConfigFile();
        saveConfigFile();
    }

    // --- ManyLib-backed getters ---
    public int getBlockLimit() { return VeinMinerConfigs.blockLimit.getIntegerValue(); }
    public int getRadiusLimit() { return VeinMinerConfigs.radiusLimit.getIntegerValue(); }
    public int getBlocksPerTick() { return VeinMinerConfigs.blocksPerTick.getIntegerValue(); }
    public int getHungerMultiplier() { return VeinMinerConfigs.hungerMultiplier.getIntegerValue(); }
    public int getExperienceMultiplier() { return VeinMinerConfigs.experienceMultiplier.getIntegerValue(); }
    public boolean getEnableAllBlocks() { return VeinMinerConfigs.enableAllBlocks.getBooleanValue(); }
    public boolean getEnableAllTools() { return VeinMinerConfigs.enableAllTools.getBooleanValue(); }
    public String getClientPreferredMode() { return VeinMinerConfigs.preferredMode.getEnumValue().toConfigString(); }

    // --- ManyLib-backed setters ---
    public void setBlockLimit(int v) { VeinMinerConfigs.blockLimit.setIntegerValue(v); }
    public void setRadiusLimit(int v) { VeinMinerConfigs.radiusLimit.setIntegerValue(v); }
    public void setBlocksPerTick(int v) { VeinMinerConfigs.blocksPerTick.setIntegerValue(v); }
    public void setHungerMultiplier(int v) { VeinMinerConfigs.hungerMultiplier.setIntegerValue(v); }
    public void setExperienceMultiplier(int v) { VeinMinerConfigs.experienceMultiplier.setIntegerValue(v); }
    public void setEnableAllBlocks(boolean v) { VeinMinerConfigs.enableAllBlocks.setBooleanValue(v); }
    public void setEnableAllTools(boolean v) { VeinMinerConfigs.enableAllTools.setBooleanValue(v); }
    public void setClientPreferredMode(String mode) { VeinMinerConfigs.preferredMode.setEnumValue(PreferredModeEnum.fromConfigString(mode)); }

    public void loadConfigFile() {
        // Load ManyLib config (config/veinminer.json)
        VeinMinerConfigs.INSTANCE.load();

        // Load tools-and-blocks.json
        try {
            if (configFileJson.exists()) {
                String toolsAndBlocksString = readFile(configFileJson);
                toolsAndBlocks = new JsonParser().parse(toolsAndBlocksString);
            } else {
                VeinMiner.instance.logger.info("tools-and-blocks.json missing. Creating.");
            }
        } catch (Exception e) {
            VeinMiner.instance.logger.error("Error parsing " + configFileJson.getName() + ": " + e.getLocalizedMessage());
        }

        // Load remaining Properties (equalBlocks, autodetect) from general.cfg
        if (configFileProps.exists()) {
            try (FileInputStream in = new FileInputStream(configFileProps)) {
                props.load(in);
            } catch (IOException e) {
                MinerLogger.debug("Could not read config: %s", e.getLocalizedMessage());
            }
        }

        for (ToolType toolType : ToolType.values()) {
            ConfigOptionBoolean autoToggle = toolConfig.get(toolType).autodetectToggle;
            autoToggle.value = Boolean.parseBoolean(props.getProperty(autoToggle.configName, String.valueOf(autoToggle.valueDefault)));
            ConfigOptionString autoList = toolConfig.get(toolType).autodetectList;
            autoList.value = props.getProperty(autoList.configName, autoList.valueDefault);
        }

        BLOCK_EQUIVALENCY_LIST = props.getProperty("equalBlocks", BLOCK_EQUIVALENCY_LIST_DEFAULT);
    }

    public void saveConfigFile() {
        // Save ManyLib config (config/veinminer.json)
        VeinMinerConfigs.INSTANCE.save();

        // Save remaining Properties (equalBlocks, autodetect)
        for (ToolType toolType : ToolType.values()) {
            ConfigOptionBoolean autoToggle = toolConfig.get(toolType).autodetectToggle;
            props.setProperty(autoToggle.configName, String.valueOf(autoToggle.value));
            ConfigOptionString autoList = toolConfig.get(toolType).autodetectList;
            props.setProperty(autoList.configName, autoList.value);
        }
        props.setProperty("equalBlocks", BLOCK_EQUIVALENCY_LIST);

        try (FileOutputStream out = new FileOutputStream(configFileProps)) {
            props.store(out, "VeinMiner configuration (equalBlocks & autodetect only; main config in veinminer.json)");
        } catch (IOException e) {
            VeinMiner.instance.logger.error("Error writing config file %s!", configFileProps.toString());
        }

        // Save tools-and-blocks.json
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String outputJson = gson.toJson(toolsAndBlocks);
        try {
            writeFile(configFileJson, outputJson);
        } catch (IOException e) {
            VeinMiner.instance.logger.error("Error writing file %s!", configFileJson.toString());
        }
    }

    private static String readFile(File f) throws IOException {
        byte[] data = new byte[(int) f.length()];
        try (FileInputStream in = new FileInputStream(f)) {
            in.read(data);
        }
        return new String(data, Charset.defaultCharset());
    }

    private static void writeFile(File f, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(content.getBytes(Charset.defaultCharset()));
        }
    }
}
