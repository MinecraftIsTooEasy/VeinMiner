package com.moddedmite.mitemod.veinminer.network;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.configuration.VeinMinerConfigs;
import com.moddedmite.mitemod.veinminer.lib.ModInfo;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.ChatMessageComponent;
import net.minecraft.EntityPlayer;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

import java.util.UUID;

/**
 * Bidirectional: changes the vein-mining mode.
 * Server → client: confirm mode change.
 * Client → server: request mode change.
 */
public class PacketChangeMode implements Packet {
    public short mode;

    public PacketChangeMode() {
        mode = 0;
    }

    public PacketChangeMode(int clientMode) {
        switch (clientMode) {
            case PreferredMode.DISABLED:
            case PreferredMode.PRESSED:
            case PreferredMode.RELEASED:
            case PreferredMode.SNEAK_ACTIVE:
            case PreferredMode.SNEAK_INACTIVE:
                mode = (short) clientMode;
                break;
            default:
                mode = 0;
        }
    }

    public PacketChangeMode(short rawMode) {
        this.mode = rawMode;
    }

    public PacketChangeMode(PacketByteBuf buf) {
        this.mode = buf.readShort();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeShort(mode);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (player instanceof ServerPlayer) {
            // Client → server: request mode change.
            ServerPlayer serverPlayer = (ServerPlayer) player;
            UUID playerName = serverPlayer.getUniqueID();
            MinerServer minerServer = VeinMiner.instance.minerServer;
            if (minerServer == null) return;
            minerServer.addClientPlayer(playerName);
            switch (this.mode) {
                case PreferredMode.DISABLED:
                case PreferredMode.PRESSED:
                case PreferredMode.RELEASED:
                    minerServer.setPlayerStatus(playerName, PlayerStatus.INACTIVE);
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.auto"));
                    break;
                case PreferredMode.SNEAK_ACTIVE:
                    minerServer.setPlayerStatus(playerName, PlayerStatus.SNEAK_ACTIVE);
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.sneak"));
                    break;
                case PreferredMode.SNEAK_INACTIVE:
                    minerServer.setPlayerStatus(playerName, PlayerStatus.SNEAK_INACTIVE);
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.nosneak"));
                    break;
            }
        } else {
            // Server → client: confirm mode change.
            VeinMiner.instance.currentMode = this.mode;
            VeinMiner.instance.logger.info(String.format("Received mode change %d", this.mode));
            String key;
            switch (this.mode) {
                case PreferredMode.PRESSED:
                    key = "command.veinminerc.set.pressed";
                    break;
                case PreferredMode.RELEASED:
                    key = "command.veinminerc.set.released";
                    break;
                default:
                    key = "command.veinminerc.set.disabled";
                    break;
            }
            if (VeinMinerConfigs.showLoginMessage.getBooleanValue()) {
                Minecraft.getMinecraft().thePlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(key));
            }
        }
    }

    @Override
    public ResourceLocation getChannel() {
        return new ResourceLocation(ModInfo.MODID, "change_mode");
    }
}
