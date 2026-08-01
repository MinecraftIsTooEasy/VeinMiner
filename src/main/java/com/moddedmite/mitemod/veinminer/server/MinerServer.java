package com.moddedmite.mitemod.veinminer.server;

import com.moddedmite.mitemod.veinminer.api.Point;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationSettings;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationValues;
import com.moddedmite.mitemod.veinminer.core.MinerInstance;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates vein mining instances, player states and tool overrides.
 */
public class MinerServer {

    private final Set<MinerInstance> minerInstances;
    private ConcurrentHashMap<ServerPlayer, MinerInstance> playerMinerInstances;
    private HashSet<UUID> clientPlayers;
    private ConcurrentHashMap<UUID, PlayerStatus> players;
    private ConfigurationSettings settings;

    public MinerServer(ConfigurationValues configValues) {
        minerInstances = Collections.synchronizedSet(new HashSet<MinerInstance>());
        playerMinerInstances = new ConcurrentHashMap<ServerPlayer, MinerInstance>();
        clientPlayers = new HashSet<UUID>();
        players = new ConcurrentHashMap<UUID, PlayerStatus>();
        settings = new ConfigurationSettings(configValues);
    }

    public void setPlayerStatus(UUID player, PlayerStatus status) {
        players.put(player, status);
    }

    public PlayerStatus getPlayerStatus(UUID player) {
        if (players.containsKey(player)) {
            return players.get(player);
        } else {
            return PlayerStatus.INACTIVE;
        }
    }

    public void addEntity(Entity entity) {
        int eX = (int) Math.floor(entity.posX);
        int eY = (int) Math.floor(entity.posY);
        int eZ = (int) Math.floor(entity.posZ);
        Point p = new Point(eX, eY, eZ);

        if (!(entity instanceof EntityItem)) {
            return;
        }
        EntityItem entityItem = (EntityItem) entity;

        synchronized (minerInstances) {
            for (MinerInstance minerInstance : minerInstances) {
                if (minerInstance.isRegistered(p)) {
                    minerInstance.addDrop(entityItem);
                }
            }
        }
    }

    public boolean awaitingDrop(Point p) {
        synchronized (minerInstances) {
            for (MinerInstance minerInstance : minerInstances) {
                if (minerInstance.isRegistered(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pointIsBlacklisted(Point point) {
        synchronized (minerInstances) {
            for (MinerInstance minerInstance : minerInstances) {
                if (minerInstance.pointIsBlacklisted(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeFromBlacklist(Point point) {
        synchronized (minerInstances) {
            for (MinerInstance minerInstance : minerInstances) {
                if (minerInstance.pointIsBlacklisted(point)) {
                    minerInstance.removeFromBlacklist(point);
                }
            }
        }
    }

    public void addInstance(MinerInstance ins) {
        synchronized (minerInstances) {
            minerInstances.add(ins);
        }
        playerMinerInstances.put(ins.getPlayer(), ins);
    }

    public MinerInstance getInstance(EntityPlayer playerMP) {
        if (playerMP instanceof ServerPlayer && playerMinerInstances.containsKey(playerMP)) {
            return playerMinerInstances.get(playerMP);
        }
        return null;
    }

    public void removeInstance(MinerInstance ins) {
        synchronized (minerInstances) {
            minerInstances.remove(ins);
        }
        if (playerMinerInstances.containsKey(ins.getPlayer())) {
            playerMinerInstances.remove(ins.getPlayer());
        }
    }

    /** Process all active instances' queues. Called each server tick. */
    public void tickInstances() {
        synchronized (minerInstances) {
            for (MinerInstance minerInstance : minerInstances) {
                minerInstance.processQueue();
            }
        }
    }

    public ConfigurationSettings getConfigurationSettings() {
        return settings;
    }

    public boolean playerHasClient(UUID playerName) {
        return clientPlayers.contains(playerName);
    }

    public void addClientPlayer(UUID playerName) {
        clientPlayers.add(playerName);
        setPlayerStatus(playerName, PlayerStatus.INACTIVE);
    }

    public void removeClientPlayer(UUID playerName) {
        clientPlayers.remove(playerName);
    }
}
