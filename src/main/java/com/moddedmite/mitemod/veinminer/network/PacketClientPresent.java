package com.moddedmite.mitemod.veinminer.network;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.configuration.VeinMinerConfigs;
import com.moddedmite.mitemod.veinminer.lib.ModInfo;
import com.moddedmite.mitemod.veinminer.lib.MinerLogger;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.ChatMessageComponent;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;

import java.util.UUID;

/**
 * Client → server: client has the mod and reports preferred mode.
 */
public class PacketClientPresent implements Packet {
    public short mode;

    public PacketClientPresent() {
        mode = 0;
    }

    public PacketClientPresent(int clientMode) {
        switch (clientMode) {
            case PreferredMode.PRESSED: mode = 1; break;
            case PreferredMode.RELEASED: mode = 2; break;
            case PreferredMode.SNEAK_ACTIVE: mode = 3; break;
            case PreferredMode.SNEAK_INACTIVE: mode = 4; break;
            default: mode = 0;
        }
    }

    public PacketClientPresent(PacketByteBuf buf) {
        this.mode = buf.readShort();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeShort(mode);
    }

    @Override
    public void apply(EntityPlayer player) {
        if (!(player instanceof ServerPlayer)) return;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        MinerLogger.debug("Received a PacketClientPresent");
        UUID playerName = serverPlayer.getUniqueID();

        MinerServer minerServer = VeinMiner.instance.minerServer;
        if (minerServer == null) return;
        minerServer.addClientPlayer(playerName);
        boolean showLoginMessage = VeinMinerConfigs.showLoginMessage.getBooleanValue();
        switch (this.mode) {
            case 3:
                minerServer.setPlayerStatus(playerName, PlayerStatus.SNEAK_ACTIVE);
                if (showLoginMessage)
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.sneak"));
                break;
            case 4:
                minerServer.setPlayerStatus(playerName, PlayerStatus.SNEAK_INACTIVE);
                if (showLoginMessage)
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.nosneak"));
                break;
            case 1:
            case 2:
                if (showLoginMessage)
                    serverPlayer.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("mod.veinminer.preferredmode.auto"));
            default:
                minerServer.setPlayerStatus(playerName, PlayerStatus.INACTIVE);
        }

        Network.sendToClient(serverPlayer, new PacketChangeMode(this.mode));
    }

    @Override
    public ResourceLocation getChannel() {
        return new ResourceLocation(ModInfo.MODID, "client_present");
    }
}
