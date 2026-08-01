package com.moddedmite.mitemod.veinminer.core;

import com.moddedmite.mitemod.veinminer.VeinMiner;
import com.moddedmite.mitemod.veinminer.api.Point;
import com.moddedmite.mitemod.veinminer.api.VeinminerHarvestFailedCheck;
import com.moddedmite.mitemod.veinminer.api.VeinminerNoToolCheck;
import com.moddedmite.mitemod.veinminer.api.VeinminerPostUseTool;
import com.moddedmite.mitemod.veinminer.configuration.ConfigurationSettings;
import com.moddedmite.mitemod.veinminer.lib.BlockLib;
import com.moddedmite.mitemod.veinminer.lib.MinerLogger;
import com.moddedmite.mitemod.veinminer.server.MinerServer;
import com.moddedmite.mitemod.veinminer.util.BlockID;
import com.moddedmite.mitemod.veinminer.util.ItemStackID;
import com.moddedmite.mitemod.veinminer.util.PlayerStatus;
import net.minecraft.Block;
import net.minecraft.ChatMessageComponent;
import net.minecraft.EntityItem;
import net.minecraft.I18n;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ServerPlayer;
import net.minecraft.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Performs the work of VeinMiner: mines a vein of blocks starting from a point.
 */
public class MinerInstance {
    public MinerServer serverInstance;
    private HashSet<Point> startBlacklist;
    private ConcurrentLinkedQueue<Point> destroyQueue;
    private HashSet<Point> awaitingEntityDrop;
    private LinkedHashMap<ItemStackID, Integer> drops;
    private World world;
    private ServerPlayer player;
    private BlockID targetBlock;
    private boolean finished;
    private ItemStack usedItem;
    private int numBlocksMined;
    private Point initalBlock;
    private int radiusLimit;
    private int blockLimit;

    private static final int MIN_HUNGER = 1;

    public MinerInstance(World world, ServerPlayer player, Point startPoint, BlockID blockID, MinerServer server, int radiusLimit, int blockLimit) {
        startBlacklist = new HashSet<Point>();
        destroyQueue = new ConcurrentLinkedQueue<Point>();
        awaitingEntityDrop = new HashSet<Point>();
        drops = new LinkedHashMap<ItemStackID, Integer>();
        this.world = world;
        this.player = player;
        targetBlock = blockID;
        finished = false;
        serverInstance = server;
        usedItem = player.getHeldItemStack();
        numBlocksMined = 1;
        initalBlock = startPoint;
        this.radiusLimit = radiusLimit;
        this.blockLimit = blockLimit;

        serverInstance.addInstance(this);
    }

    public void cleanUp() {
        // No event bus unregister needed; central tick handler manages instances.
    }

    private boolean shouldContinue() {
        if (!serverInstance.getConfigurationSettings().getEnableAllTools() && player.getHeldItemStack() == null) {
            VeinminerNoToolCheck toolCheck = new VeinminerNoToolCheck(player);
            // No external event bus; default DENY means open hand won't work unless enableAllTools.
            if (toolCheck.allowTool.isAllowed() || player.capabilities.isCreativeMode) {
                this.finished = false;
            } else if (toolCheck.allowTool.isDenied()) {
                this.finished = true;
            } else {
                this.finished = true;
            }
        }

        if (usedItem == null) {
            if (player.getHeldItemStack() != null) {
                this.finished = true;
            }
        } else if (player.getHeldItemStack() == null || !ItemStack.areItemStacksEqual(player.getHeldItemStack(), usedItem)) {
            this.finished = true;
        }

        UUID playerName = player.getUniqueID();
        PlayerStatus playerStatus = serverInstance.getPlayerStatus(playerName);
        if (playerStatus == null) {
            this.finished = true;
        } else if (playerStatus == PlayerStatus.INACTIVE ||
                (playerStatus == PlayerStatus.SNEAK_ACTIVE && !player.isSneaking()) ||
                (playerStatus == PlayerStatus.SNEAK_INACTIVE && player.isSneaking())) {
            this.finished = true;
        }

        if (finished) {
            return false;
        }

        net.minecraft.FoodStats food = player.getFoodStats();
        if (food.getSatiation() < MIN_HUNGER) {
            this.finished = true;
            sendFinishedMessage("mod.veinminer.finished.tooHungry");
        }

        int experienceMod = serverInstance.getConfigurationSettings().getExperienceMultiplier();
        if (experienceMod > 0 && player.experience < experienceMod) {
            this.finished = true;
            if (player.experience < 0) player.experience = 0;
            sendFinishedMessage("mod.veinminer.finished.noExp");
        }

        if (numBlocksMined >= blockLimit && blockLimit != -1) {
            MinerLogger.debug("Blocks mined: %d; Blocklimit: %d. Forcing finish.", numBlocksMined, blockLimit);
            this.finished = true;
        }

        return !this.finished;
    }

    private void sendFinishedMessage(String problem) {
        if (serverInstance.playerHasClient(player.getUniqueID())) {
            player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey(problem));
        } else {
            String translatedProblem = I18n.getString(problem);
            player.sendChatToPlayer(ChatMessageComponent.createFromText(translatedProblem));
        }
    }

    private boolean toolAllowedForBlock(ItemStack tool, BlockID block) {
        boolean toolAllowed = false;
        ConfigurationSettings settings = serverInstance.getConfigurationSettings();
        for (String type : settings.getToolTypeNames()) {
            if (settings.toolIsOfType(tool, type)) {
                if (settings.whiteListHasBlockId(type, block)) {
                    toolAllowed = true;
                }
            }
        }
        return toolAllowed;
    }

    private void takeHunger() {
        float hungerMod = ((float) serverInstance.getConfigurationSettings().getHungerMultiplier()) * 0.025F;
        net.minecraft.FoodStats s = player.getFoodStats();
        s.addHungerServerSide(hungerMod);
    }

    private void takeExperience() {
        int expToTakeAway = serverInstance.getConfigurationSettings().getExperienceMultiplier();
        if (expToTakeAway == 0) {
            return;
        }
        player.addExperience(-expToTakeAway);
    }

    public int mineBlock(Point point) {
        return mineBlock(point.getX(), point.getY(), point.getZ());
    }

    private int mineBlock(int x, int y, int z) {
        int mineSuccessful = 0;
        Point newPoint = new Point(x, y, z);
        Block block = world.getBlock(x, y, z);
        if (block == null) {
            return mineSuccessful;
        }
        int meta = world.getBlockMetadata(x, y, z);
        BlockID newBlock = new BlockID(block.blockID, meta);
        ConfigurationSettings configurationSettings = serverInstance.getConfigurationSettings();
        startBlacklist.add(newPoint);
        if (mineAllowed(newBlock, newPoint, configurationSettings)) {
            mineSuccessful = mineSuccessful | 1;
            awaitingEntityDrop.add(newPoint);
            boolean success = player.theItemInWorldManager.tryHarvestBlock(x, y, z);
            numBlocksMined++;

            if (!player.capabilities.isCreativeMode) {
                takeHunger();
                takeExperience();
            }

            VeinminerPostUseTool toolUsedEvent = new VeinminerPostUseTool(player, newPoint);
            // No external event bus; default no-op.

            VeinminerHarvestFailedCheck continueCheck = new VeinminerHarvestFailedCheck(player, newPoint, targetBlock.name, targetBlock.metadata);
            if (success || continueCheck.allowContinue.isAllowed()) {
                mineSuccessful = mineSuccessful | 2;
                postSuccessfulBreak(newPoint);
                awaitingEntityDrop.remove(newPoint);
            } else {
                awaitingEntityDrop.remove(newPoint);
            }
        }
        return mineSuccessful;
    }

    public void postSuccessfulBreak(Point breakPoint) {
        ArrayList<Point> surroundingPoints = getPoints(breakPoint);
        destroyQueue.addAll(surroundingPoints);
    }

    private ArrayList<Point> getPoints(Point origin) {
        ArrayList<Point> points = new ArrayList<Point>(9);
        int[] dimRange = {-1, 0, 1};
        for (int dx : dimRange) {
            for (int dy : dimRange) {
                for (int dz : dimRange) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    points.add(new Point(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz));
                }
            }
        }
        Collections.shuffle(points);
        return points;
    }

    private boolean mineAllowed(BlockID newBlock, Point newBlockPos, ConfigurationSettings configSettings) {
        if (finished || !shouldContinue()) return false;
        if (newBlock.getBlock() == null) {
            return false;
        }
        if (!newBlock.wildcardEquals(targetBlock) && !configSettings.areBlocksCongruent(newBlock, targetBlock)
                && !BlockLib.arePickBlockEqual(newBlock, targetBlock)) {
            return false;
        }
        if (!newBlockPos.isWithinRange(initalBlock, radiusLimit) && radiusLimit > 0) {
            MinerLogger.debug("Out of radius.");
            return false;
        }
        if (awaitingEntityDrop.contains(newBlockPos))
            return false;
        if (numBlocksMined >= blockLimit && blockLimit != -1) {
            MinerLogger.debug("Block limit is: %d; Blocks mined: %d", blockLimit, numBlocksMined);
            return false;
        }
        boolean result = (configSettings.getEnableAllBlocks() || toolAllowedForBlock(usedItem, newBlock));
        return result;
    }

    /**
     * Process the destroy queue for this tick. Called centrally by MinerServer.
     */
    public void processQueue() {
        int quantity = serverInstance.getConfigurationSettings().getBlocksPerTick();
        int i = 0;
        while (i < quantity) {
            if (!destroyQueue.isEmpty()) {
                Point target = destroyQueue.remove();
                if ((mineBlock(target.getX(), target.getY(), target.getZ()) & 2) == 2) {
                    i += 1;
                }
            } else {
                serverInstance.removeInstance(this);
                if (!drops.isEmpty()) {
                    spawnDrops();
                }
                cleanUp();
                return;
            }
        }
    }

    private void spawnDrops() {
        for (Map.Entry<ItemStackID, Integer> schedDrop : drops.entrySet()) {
            ItemStackID itemStack = schedDrop.getKey();
            Item foundItem = itemStack.getItem();
            if (foundItem == null) {
                continue;
            }
            int itemDamage = itemStack.getDamage();
            int numItems = schedDrop.getValue();
            int max = itemStack.getMaxStackSize();
            if (max <= 0) max = 64;
            while (numItems > max) {
                ItemStack newItemStack = new ItemStack(foundItem, max, itemDamage);
                EntityItem newEntityItem = new EntityItem(world, initalBlock.getX() + 0.5F, initalBlock.getY() + 0.5F, initalBlock.getZ() + 0.5F, newItemStack);
                world.spawnEntityInWorld(newEntityItem);
                numItems -= max;
            }
            ItemStack newItemStack = new ItemStack(foundItem, numItems, itemDamage);
            newItemStack.setItemDamage(itemDamage);
            EntityItem newEntityItem = new EntityItem(world, initalBlock.getX() + 0.5F, initalBlock.getY() + 0.5F, initalBlock.getZ() + 0.5F, newItemStack);
            world.spawnEntityInWorld(newEntityItem);
        }
        drops.clear();
    }

    public boolean isRegistered(Point p) {
        return awaitingEntityDrop.contains(p);
    }

    public void addDrop(EntityItem entity) {
        ItemStack item = entity.getEntityItem();
        ItemStackID itemInfo = new ItemStackID(item);
        if (drops.containsKey(itemInfo)) {
            int oldDropNumber = drops.get(itemInfo);
            int newDropNumber = oldDropNumber + item.stackSize;
            drops.put(itemInfo, newDropNumber);
        } else {
            drops.put(itemInfo, item.stackSize);
        }
    }

    public boolean pointIsBlacklisted(Point point) {
        return startBlacklist.contains(point);
    }

    public void removeFromBlacklist(Point point) {
        if (startBlacklist.contains(point)) {
            startBlacklist.remove(point);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinerInstance that = (MinerInstance) o;
        if (radiusLimit != that.radiusLimit) return false;
        if (blockLimit != that.blockLimit) return false;
        if (serverInstance != null ? !serverInstance.equals(that.serverInstance) : that.serverInstance != null) return false;
        if (world != null ? !world.equals(that.world) : that.world != null) return false;
        if (player != null ? !player.equals(that.player) : that.player != null) return false;
        if (usedItem != null ? !usedItem.equals(that.usedItem) : that.usedItem != null) return false;
        return initalBlock != null ? initalBlock.equals(that.initalBlock) : that.initalBlock == null;
    }

    @Override
    public int hashCode() {
        int result = serverInstance != null ? serverInstance.hashCode() : 0;
        result = 31 * result + (world != null ? world.hashCode() : 0);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        result = 31 * result + (usedItem != null ? usedItem.hashCode() : 0);
        result = 31 * result + (initalBlock != null ? initalBlock.hashCode() : 0);
        result = 31 * result + radiusLimit;
        result = 31 * result + blockLimit;
        return result;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
