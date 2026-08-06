package com.moddedmite.mitemod.veinminer.util;

import net.minecraft.Item;
import net.minecraft.ItemStack;

public class ItemStackID {
    private String itemId;
    private int damage;
    private int subtype;
    private int maxStackSize;

    public ItemStackID(String id, int dam, int sub, int stackSize) {
        itemId = id;
        damage = dam;
        subtype = sub;
        maxStackSize = stackSize;
    }

    public ItemStackID(String id, int dam, int stackSize) {
        this(id, dam, 0, stackSize);
    }

    public ItemStackID(Item item, int damage, int stackSize) {
        this(String.valueOf(item.itemID), damage, 0, stackSize);
    }

    public ItemStackID(ItemStack stack) {
        this(String.valueOf(stack.getItem().itemID),
                stack.getItemDamage(),
                stack.getItemSubtype(),
                stack.getMaxStackSize());
    }

    public String getItemId() {
        return itemId;
    }

    public int getDamage() {
        return damage;
    }

    public int getSubtype() {
        return subtype;
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
        output += subtype << 8;
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ItemStackID)) return false;
        ItemStackID rhs = (ItemStackID) obj;
        return (itemId.equals(rhs.itemId) && damage == rhs.damage && subtype == rhs.subtype);
    }
}
