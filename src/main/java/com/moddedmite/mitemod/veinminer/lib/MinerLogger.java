package com.moddedmite.mitemod.veinminer.lib;

import moddedmite.rustedironcore.api.util.LogUtil;

public class MinerLogger {
    public static void debug(String format, Object... data) {
        if (ModInfo.DEBUG_MODE) {
            LogUtil.getLogger().info(String.format(format, data));
        }
    }
}
