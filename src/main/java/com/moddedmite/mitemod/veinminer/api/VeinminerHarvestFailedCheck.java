package com.moddedmite.mitemod.veinminer.api;

import net.minecraft.EntityPlayer;

public class VeinminerHarvestFailedCheck {
    public EntityPlayer player;
    public Point blockPoint;
    public String targetBlockName;
    public int targetBlockMetadata;
    public Permission allowContinue = Permission.DENY;

    public VeinminerHarvestFailedCheck(EntityPlayer player, Point blockPoint, String targetBlockName, int targetBlockMetadata) {
        this.player = player;
        this.blockPoint = blockPoint;
        this.targetBlockName = targetBlockName;
        this.targetBlockMetadata = targetBlockMetadata;
    }
}
