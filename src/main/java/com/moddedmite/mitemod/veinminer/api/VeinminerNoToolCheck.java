package com.moddedmite.mitemod.veinminer.api;

import net.minecraft.EntityPlayer;

public class VeinminerNoToolCheck {
    public EntityPlayer player;
    public Permission allowTool = Permission.DENY;

    public VeinminerNoToolCheck(EntityPlayer player) {
        this.player = player;
    }
}
