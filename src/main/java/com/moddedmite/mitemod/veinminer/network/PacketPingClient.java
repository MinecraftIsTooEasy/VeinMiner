package com.moddedmite.mitemod.veinminer.network;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.lib.ModInfo;
import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.Packet;
import moddedmite.rustedironcore.network.PacketByteBuf;
import net.minecraft.EntityPlayer;
import net.minecraft.ResourceLocation;

/**
 * Server → client ping on login. Client responds with PacketClientPresent.
 */
public class PacketPingClient implements Packet {
    public PacketPingClient() {}

    public PacketPingClient(PacketByteBuf buf) {}

    @Override
    public void write(PacketByteBuf buf) {}

    @Override
    public void apply(EntityPlayer player) {
        // Client side: respond with client present packet carrying preferred mode.
        int preferredMode = VeinMiner.instance.configurationSettings != null
                ? VeinMiner.instance.configurationSettings.getPreferredMode()
                : 1;
        Network.sendToServer(new PacketClientPresent(preferredMode));
        VeinMiner.instance.proxy.resetKeybindPacketCount();
    }

    @Override
    public ResourceLocation getChannel() {
        return new ResourceLocation(ModInfo.MODID, "ping_client");
    }
}
