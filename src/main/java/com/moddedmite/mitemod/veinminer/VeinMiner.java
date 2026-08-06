package com.moddedmite.mitemod.veinminer;

import com.moddedmite.mitemod.veinminer.client.ClientCommand;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationSettings;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationValues;
import com.moddedmite.mitemod.veinminer.configuration.VeinMinerConfigs;
import com.moddedmite.mitemod.veinminer.lib.MinerLogger;
import com.moddedmite.mitemod.veinminer.lib.ModInfo;
import com.moddedmite.mitemod.veinminer.network.PacketChangeMode;
import com.moddedmite.mitemod.veinminer.network.PacketClientPresent;
import com.moddedmite.mitemod.veinminer.network.PacketMinerActivate;
import com.moddedmite.mitemod.veinminer.network.PacketPingClient;
import com.moddedmite.mitemod.veinminer.proxy.ClientProxy;
import com.moddedmite.mitemod.veinminer.proxy.CommonProxy;
import com.moddedmite.mitemod.veinminer.server.MinerCommand;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.PreferredMode;
import moddedmite.rustedironcore.api.event.Handlers;
import moddedmite.rustedironcore.api.event.events.CommandRegisterEvent;
import moddedmite.rustedironcore.api.event.events.PlayerLoggedInEvent;
import moddedmite.rustedironcore.api.event.events.PlayerLoggedOutEvent;
import moddedmite.rustedironcore.api.event.listener.IInitializationListener;
import moddedmite.rustedironcore.api.event.listener.IPlayerEventListener;
import moddedmite.rustedironcore.api.event.listener.ITickListener;
import moddedmite.rustedironcore.api.util.FabricUtil;
import moddedmite.rustedironcore.api.util.LogUtil;
import moddedmite.rustedironcore.network.Network;
import moddedmite.rustedironcore.network.PacketReader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.ResourceLocation;
import net.minecraft.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.xiaoyu233.fml.ModResourceManager;
import fi.dy.masa.malilib.config.ConfigManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Main mod entrypoint for VeinMiner on MITE (FishModLoader + RustedIronCore).
 *
 * <p>Replaces the Forge {@code @Mod} class. Responsibilities:</p>
 * <ul>
 *   <li>Register network packets with RIC's {@link PacketReader}.</li>
 *   <li>Register event listeners for player login/logout, server tick and
 *       server initialization.</li>
 *   <li>Register the {@code /veinminer} and {@code /veinminerc} commands.</li>
 *   <li>Load configuration and create the {@link MinerServer}.</li>
 *   <li>Own the client proxy when running on a client.</li>
 * </ul>
 */
public class VeinMiner implements ModInitializer {
    public static final String MOD_ID = ModInfo.MODID;

    public static VeinMiner instance;

    public CommonProxy proxy = new CommonProxy();
    public MinerServer minerServer = null;
    public int currentMode = PreferredMode.DISABLED;
    public Logger logger;
    public ConfigurationSettings configurationSettings;
    private ConfigurationValues configurationValues;

    @Override
    public void onInitialize() {
        instance = this;
        logger = LogUtil.getLogger();

        setupClientProxy();

        // Initialise and register ManyLib-backed config (config/veinminer.json).
        // Must happen before loadConfiguration() since ConfigurationValues
        // reads from VeinMinerConfigs.INSTANCE.
        new VeinMinerConfigs();
        ConfigManager.getInstance().registerConfig(VeinMinerConfigs.INSTANCE);

        loadConfiguration();
        // MinerServer depends on configuration; create it early so command
        // registration (which happens during server init) can reference it.
        minerServer = new MinerServer(configurationValues);
        proxy.setMinerServer(minerServer);

        registerPackets();
        registerListeners();

        logger.info("VeinMiner initialized.");
    }

    private void registerPackets() {
        ResourceLocation changeMode = new ResourceLocation(ModInfo.MODID, "change_mode");
        ResourceLocation clientPresent = new ResourceLocation(ModInfo.MODID, "client_present");
        ResourceLocation minerActivate = new ResourceLocation(ModInfo.MODID, "miner_activate");
        ResourceLocation pingClient = new ResourceLocation(ModInfo.MODID, "ping_client");

        // Server reads packets that clients send to it.
        PacketReader.registerServerPacketReader(changeMode, PacketChangeMode::new);
        PacketReader.registerServerPacketReader(clientPresent, PacketClientPresent::new);
        PacketReader.registerServerPacketReader(minerActivate, PacketMinerActivate::new);

        // Client reads packets that the server sends to it.
        PacketReader.registerClientPacketReader(changeMode, PacketChangeMode::new);
        PacketReader.registerClientPacketReader(pingClient, PacketPingClient::new);
    }

    private void registerListeners() {
        // Player login/logout: ping client on login, clean up on logout.
        Handlers.PlayerEvent.register(new IPlayerEventListener() {
            @Override
            public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
                if (minerServer == null) return;
                ServerPlayer player = event.player();
                Network.sendToClient(player, new PacketPingClient());
                MinerLogger.debug("Sent ping packet to client");
            }

            @Override
            public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
                if (minerServer == null) return;
                minerServer.removeClientPlayer(event.player().getUniqueID());
            }
        });

        // Server tick: process active miner instances.
        Handlers.Tick.register(new ITickListener() {
            @Override
            public void onServerTick(MinecraftServer server) {
                if (minerServer != null) {
                    minerServer.tickInstances();
                }
            }
        });

        // Server initialization: ensure MinerServer is ready.
        Handlers.Initialization.register(new IInitializationListener() {
            @Override
            public void onServerStarted(MinecraftServer server) {
                if (minerServer == null && configurationValues != null) {
                    minerServer = new MinerServer(configurationValues);
                    proxy.setMinerServer(minerServer);
                }
                MinerLogger.debug("MinerServer ready.");
            }
        });

        // Command registration: /veinminer and /veinminerc.
        Handlers.Command.register(new Consumer<CommandRegisterEvent>() {
            @Override
            public void accept(CommandRegisterEvent event) {
                if (minerServer != null) {
                    event.register(new MinerCommand(minerServer));
                }
                event.register(new ClientCommand());
            }
        });
    }

    private void setupClientProxy() {
        if (!FabricUtil.isServer()) {
            // Register the mod's resource pack domain so MITE's I18n can load
            // assets/veinminer/lang/*.lang and translate our keys (command
            // messages, keybinding names/categories, etc.).
            ModResourceManager.addResourcePackDomain(ModInfo.MODID);
            proxy = new ClientProxy();
            proxy.registerClientEvents();
        }
    }

    private void loadConfiguration() {
        Path configDir = FabricUtil.getConfigDirectory();
        File modDir = new File(configDir.toFile(), "veinminer");
        if (!modDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            modDir.mkdir();
        }
        configurationValues = new ConfigurationValues(
                new File(modDir, "general.cfg"),
                new File(modDir, "tools-and-blocks.json")
        );
        configurationSettings = new ConfigurationSettings(configurationValues);
    }
}
