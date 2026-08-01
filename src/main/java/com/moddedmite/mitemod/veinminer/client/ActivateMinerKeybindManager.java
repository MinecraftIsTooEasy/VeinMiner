package com.moddedmite.mitemod.veinminer.client;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.network.PacketMinerActivate;
import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.listener.IKeybindingListener;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import moddedmite.rustedironcore.keybinding.KeyBindingExtra;
import moddedmite.rustedironcore.network.Network;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.KeyBinding;
import net.minecraft.Minecraft;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Manages the client-side keybinding that activates vein mining.
 * Registers the key with RIC's keybinding handler and watches key state
 * each client tick, sending {@link PacketMinerActivate} to the server when
 * the activation state changes.
 *
 * <p>Replaces Forge's {@code ClientRegistry.registerKeybinding} and
 * {@code InputEvent.KeyInputEvent} from the original mod.</p>
 */
@Environment(EnvType.CLIENT)
public class ActivateMinerKeybindManager implements IKeybindingListener, ITickListener {

    /** Default key: grave accent (`) — same as the original mod's KEY_GRAVE. */
    private static final int DEFAULT_KEY = 41;

    public KeyBindingExtra keyBinding;
    private static boolean statusEnabled = false;
    private final int[] count = {0, 0, 0};
    private static final int PACKET_COUNT = 5;

    public ActivateMinerKeybindManager() {
        keyBinding = new KeyBindingExtra("veinminer.key.enable", DEFAULT_KEY, "veinminer.key.category");
        registerBetterGameSettingCategory();
        Handlers.Keybinding.register(this);
        Handlers.Tick.register(this);
    }

    /**
     * Registers the keybinding category with BetterGameSetting so that the
     * key appears under the "VeinMiner" group in BGS's controls screen.
     * Uses reflection so VeinMiner does not hard-depend on BGS; if BGS is
     * not installed the key simply shows under "uncategorized".
     */
    private void registerBetterGameSettingCategory() {
        try {
            Class<?> bgsClass = Class.forName("moddedmite.xylose.bettergamesetting.client.KeyBindingExtra");
            Method method = bgsClass.getMethod("setKeyKeyCategory", String.class, String.class);
            method.invoke(null, keyBinding.getKeyDescription(), keyBinding.getKeyCategory());
        } catch (Throwable ignored) {
            // BetterGameSetting not installed; skip BGS integration
        }
    }

    @Override
    public void onKeybindingRegister(Consumer<KeyBinding> registry) {
        registry.accept(keyBinding);
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.thePlayer == null) {
            return;
        }
        boolean sendPacket = false;

        int mode = VeinMiner.instance.currentMode;
        boolean pressed = keyBinding.pressed;

        if (mode == PreferredMode.DISABLED) {
            if (statusEnabled) {
                statusEnabled = false;
            }
            if (count[0] < PACKET_COUNT) {
                sendPacket = true;
                count[0]++;
                count[1] = 0;
                count[2] = 0;
            }
        } else if ((pressed && mode == PreferredMode.PRESSED) || (!pressed && mode == PreferredMode.RELEASED) && !statusEnabled) {
            statusEnabled = true;
            if (count[1] < PACKET_COUNT) {
                sendPacket = true;
                count[0] = 0;
                count[1]++;
                count[2] = 0;
            }
        } else if (((pressed && mode == PreferredMode.RELEASED) || (!pressed && mode == PreferredMode.PRESSED)) && statusEnabled) {
            statusEnabled = false;
            if (count[2] < PACKET_COUNT) {
                sendPacket = true;
                count[0] = 0;
                count[1] = 0;
                count[2]++;
            }
        }

        if (sendPacket) {
            Network.sendToServer(new PacketMinerActivate(statusEnabled));
        }
    }

    public void resetCount() {
        count[0] = 0;
        count[1] = 0;
        count[2] = 0;
    }
}
