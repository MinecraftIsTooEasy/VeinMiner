package com.moddedmite.mitemod.veinminer.network;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.lib.ModInfo;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

import java.util.UUID;

/**
 * Client → server: activate/deactivate vein mining for the player (keybind).
 */
public class PacketMinerActivate implements Packet {
    public boolean keyActive;

    public PacketMinerActivate() {}

    public PacketMinerActivate(boolean keyActive) {
        this.keyActive = keyActive;
    }

    public PacketMinerActivate(PacketByteBuf buf) {
        this.keyActive = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(keyActive);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (!(player instanceof ServerPlayer)) return;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        UUID playerName = serverPlayer.getUniqueID();

        MinerServer minerServer = VeinMiner.instance.minerServer;
        if (minerServer == null) return;
        PlayerStatus status = minerServer.getPlayerStatus(playerName);
        if (this.keyActive) {
            if (status == PlayerStatus.INACTIVE) {
                minerServer.setPlayerStatus(playerName, PlayerStatus.ACTIVE);
            }
        } else {
            if (status == PlayerStatus.ACTIVE) {
                minerServer.setPlayerStatus(playerName, PlayerStatus.INACTIVE);
            }
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return new ResourceLocation(ModInfo.MODID, "miner_activate");
    }
}
