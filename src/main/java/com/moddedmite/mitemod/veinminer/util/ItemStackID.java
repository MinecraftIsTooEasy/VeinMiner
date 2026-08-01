package com.moddedmite.mitemod.veinminer.util;

import net.minecraft.Item;
import net.minecraft.ItemStack;

public class ItemStackID {
    private String itemId;
    private int damage;
    private int maxStackSize;

    public ItemStackID(String id, int dam, int stackSize) {
        itemId = id;
        damage = dam;
        maxStackSize = stackSize;
    }

    public ItemStackID(Item item, int damage, int stackSize) {
        this(String.valueOf(item.itemID), damage, stackSize);
    }

    public ItemStackID(ItemStack stack) {
        this(stack.getItem(), stack.getItemDamage(), stack.getMaxStackSize());
    }

    public String getItemId() {
        return itemId;
    }

    public int getDamage() {
        return damage;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public Item getItem() {
        try {
            int id = Integer.parseInt(itemId);
            return Item.itemsList[id];
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int output = 0;
        output += itemId.hashCode() << 16;
        output += damage;
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ItemStackID)) return false;
        ItemStackID rhs = (ItemStackID) obj;
        return (itemId.equals(rhs.itemId) && damage == rhs.damage);
    }
}
