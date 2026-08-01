package com.moddedmite.mitemod.veinminer.util;

import net.minecraft.Block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores the block ID and metadata of a block. MITE identifies blocks by integer
 * IDs, so `name` holds the string form of the block id. Allows wildcard metadata
 * (-1) to match blocks with any metadata value.
 */
public class BlockID implements Comparable<BlockID> {
    public String name;
    public int metadata;
    public int blockIdCache;

    public BlockID(String fullDescription) {
        int preMeta = -1;
        Pattern p = Pattern.compile("([^/]+)(?:/([-]?\\d{1,3}))?");
        Matcher m = p.matcher(fullDescription);
        if (m.matches()) {
            name = m.group(1);
            if (m.group(2) != null) {
                try {
                    preMeta = Integer.parseInt(m.group(2));
                } catch (NumberFormatException e) {
                    preMeta = -1;
                }
            }
        } else {
            name = "";
        }
        metadata = preMeta >= -1 ? preMeta : -1;
        blockIdCache = parseId(name);
    }

    public BlockID(String name, int meta) {
        this.name = name;
        this.metadata = meta < -1 ? -1 : meta;
        this.blockIdCache = parseId(name);
    }

    public BlockID(int blockId, int meta) {
        this.name = String.valueOf(blockId);
        this.metadata = meta < -1 ? -1 : meta;
        this.blockIdCache = blockId;
    }

    public BlockID(Block block, int meta) {
        this(block != null ? block.blockID : 0, meta);
    }

    private static int parseId(String name) {
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public Block getBlock() {
        return Block.getBlock(blockIdCache);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockID)) return false;
        BlockID o = (BlockID) obj;
        return name.equals(o.name) && metadata == o.metadata;
    }

    public boolean wildcardEquals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockID)) return false;
        BlockID o = (BlockID) obj;
        if (o.metadata == -1 || metadata == -1)
            return this.equals(obj);
        else
            return name.equals(o.name) && metadata == o.metadata;
    }

    @Override
    public int hashCode() {
        return (this.name.hashCode() << 6) + this.metadata;
    }

    @Override
    public String toString() {
        return (metadata == -1 ? name + "" : name + "/" + metadata);
    }

    @Override
    public int compareTo(BlockID blockID) {
        if (name != null && !name.equals(blockID.name)) {
            int result = name.compareTo(blockID.name);
            if (result > 0) return 1;
            else if (result < 0) return -1;
            return 0;
        } else if (metadata < blockID.metadata) {
            return -1;
        } else if (metadata > blockID.metadata) {
            return 1;
        } else {
            return 0;
        }
    }
}
