/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.utils;

import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemStackUtils {

    public static Identifier getOreTag(ItemStack stack) {
        Item item = stack.getItem();
        for (Identifier resourceLocation : stack.typeHolder().tags().map(TagKey::location).toList()) {
            if (resourceLocation.toString().startsWith("c:raw_materials/")) {
                return resourceLocation;
            }
        }
        return null;
    }

    public static boolean isInventoryFull(InventoryComponent<?> handler) {
        for (int i = 0; i < handler.getSlots(); ++i) {
            if (handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    // Titanium's 26.3 inventories return copies from getStackInSlot, so shrinking must write the slot back
    public static void shrink(InventoryComponent<?> handler, int slot, int amount) {
        ItemStack stack = handler.getStackInSlot(slot);
        handler.setStackInSlot(slot, stack.getCount() > amount ? stack.copyWithCount(stack.getCount() - amount) : ItemStack.EMPTY);
    }

    /**
     * Machine-internal insert into one of the machine's own inventories (a work result going into its output).
     * Titanium's {@code InventoryComponent.isValid} applies the slot input filter to every insert, and the
     * outputs use {@code setInputFilter(false)} to keep neighbours from inserting, so a plain
     * {@code ItemUtil.insertItemReturnRemaining(output, ...)} always fails. The filter is lifted for this call only.
     */
    public static ItemStack insertInternal(InventoryComponent<?> inventory, ItemStack stack, boolean simulate) {
        var filter = inventory.getInsertPredicate();
        inventory.setInputFilter((s, slot) -> true);
        try {
            return ItemUtil.insertItemReturnRemaining(inventory, stack, simulate, null);
        } finally {
            inventory.setInputFilter(filter);
        }
    }

    public static boolean isChorusFlower(ItemStack stack) {
        return !Block.byItem(stack.getItem()).equals(Blocks.AIR) && Block.byItem(stack.getItem()).equals(Blocks.CHORUS_FLOWER);
    }

    public static boolean acceptsFluidItem(ItemStack stack) {
        return !stack.isEmpty() && ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM) != null;// && !stack.getItem().equals(ForgeModContainer.getInstance().universalBucket);
    }

}
