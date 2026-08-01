package com.moddedmite.mitemod.veinminer.lib;

import com.moddedmite.mitemod.veinminer.util.BlockID;
import net.minecraft.Block;

public class BlockLib {

    /**
     * Checks if two blocks drop the same item subtype via getItemSubtype
     * (MITE replaces Forge's Block.damageDropped with Block.getItemSubtype).
     */
    public static boolean arePickBlockEqual(BlockID first, BlockID second) {
        if (first == null || second == null) {
            return false;
        }
        Block firstBlock = first.getBlock();
        Block secondBlock = second.getBlock();
        if (firstBlock == null || secondBlock == null) {
            return false;
        }
        int firstResultMeta = firstBlock.getItemSubtype(first.metadata < 0 ? 0 : first.metadata);
        int secondResultMeta = secondBlock.getItemSubtype(second.metadata < 0 ? 0 : second.metadata);
        return first.name.equals(second.name) && firstResultMeta == secondResultMeta;
    }
}
