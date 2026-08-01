package com.moddedmite.mitemod.veinminer.api;

import net.minecraft.EntityPlayer;

public class VeinminerPostUseTool {
    public EntityPlayer player;
    public Point blockPoint;

    public VeinminerPostUseTool(EntityPlayer player, Point blockPoint) {
        this.player = player;
        this.blockPoint = blockPoint;
    }
}
