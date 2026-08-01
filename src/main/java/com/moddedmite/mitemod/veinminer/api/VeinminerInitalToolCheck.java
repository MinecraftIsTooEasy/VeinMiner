package com.moddedmite.mitemod.veinminer.api;

import net.minecraft.EntityPlayer;

/**
 * Event fired before vein mining starts. Allows external modification of limits.
 * In this MITE port, kept as a simple holder (no event bus). Defaults allow start.
 */
public class VeinminerInitalToolCheck {
    public EntityPlayer player;
    public Point blockPoint;
    public int radiusLimit;
    public int blockLimit;
    public Permission allowVeinminerStart = Permission.ALLOW;

    public VeinminerInitalToolCheck(EntityPlayer player, Point blockPoint, int radiusLimit, int blockLimit, int originalRadius, int originalBlock) {
        this.player = player;
        this.blockPoint = blockPoint;
        this.radiusLimit = radiusLimit;
        this.blockLimit = blockLimit;
    }
}
