package com.moddedmite.mitemod.veinminer.proxy;

import com.moddedmite.mitemod.veinminer.client.ActivateMinerKeybindManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client-side proxy. Owns the keybinding manager and relays server state to it.
 */
@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {
    private ActivateMinerKeybindManager keybindManager;

    @Override
    public void registerClientEvents() {
        keybindManager = new ActivateMinerKeybindManager();
    }

    @Override
    public void resetKeybindPacketCount() {
        if (keybindManager != null) {
            keybindManager.resetCount();
        }
    }
}
