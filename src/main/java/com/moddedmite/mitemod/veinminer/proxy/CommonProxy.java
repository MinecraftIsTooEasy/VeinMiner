package com.moddedmite.mitemod.veinminer.proxy;

import com.moddedmite.mitemod.veinminer.server.MinerServer;

/**
 * Common (server) proxy. Holds references to server-side helpers that are
 * also referenced from shared code paths.
 */
public class CommonProxy {
    public void registerClientEvents() {
        // No-op on server side.
    }

    public void registerCommonEvents() {
        // Listeners are registered directly in VeinMiner onInitialize.
    }

    public void setMinerServer(MinerServer server) {
        // No-op on server side; client proxy uses this to relay server state.
    }

    public void resetKeybindPacketCount() {
        // No-op on server side.
    }

    public void registerPostinitCommands() {
        // No-op on server side; commands are registered via CommandRegisterEvent.
    }
}
