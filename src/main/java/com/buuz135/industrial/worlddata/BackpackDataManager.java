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

package com.buuz135.industrial.worlddata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BackpackDataManager extends SavedData {
    public static HashMap<String, BackpackItemHandler> CLIENT_SIDE_BACKPACKS = new HashMap<>();

    public static final String NAME = "IFBackpack";
    public static final int SLOT_AMOUNT = 4 * 8;
    public HashMap<String, BackpackItemHandler> itemHandlers;

    public BackpackDataManager(String name) {
        itemHandlers = new HashMap<>();
    }

    public BackpackDataManager() {
        itemHandlers = new HashMap<>();
    }

    public void createBackPack(UUID uuid) {
        this.itemHandlers.put(uuid.toString(), new BackpackItemHandler(this));
        setDirty();
    }

    public BackpackItemHandler getBackpack(String id) {
        return itemHandlers.get(id);
    }

    public static BackpackDataManager load(HolderLookup.Provider provider, CompoundTag nbt) {
        BackpackDataManager manager = new BackpackDataManager();
        manager.itemHandlers = new HashMap<>();
        CompoundTag backpacks = nbt.getCompoundOrEmpty("Backpacks");
        for (String s : backpacks.keySet()) {
            BackpackItemHandler hander = new BackpackItemHandler(manager);
            hander.deserializeNBT(provider, backpacks.getCompoundOrEmpty(s));
            manager.itemHandlers.put(s, hander);
        }

        return manager;
    }

    @Nullable
    public static BackpackDataManager getData(LevelAccessor world) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD);
            BackpackDataManager data = serverWorld.getDataStorage().computeIfAbsent(
                    new Factory<>(BackpackDataManager::new, (compoundTag, provider) -> BackpackDataManager.load(provider, compoundTag)), NAME);
            return data;
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT(provider)));
        compound.put("Backpacks", nbt);
        return compound;
    }

    public static class BackpackItemHandler implements IItemHandler {

        private List<SlotDefinition> definitionList;
        private int maxAmount;
        private BackpackDataManager dataManager;

        public BackpackItemHandler(BackpackDataManager manager) {
            definitionList = new ArrayList<>();
            for (int i = 0; i < SLOT_AMOUNT; i++) {
                definitionList.add(new SlotDefinition(this));
            }
            this.maxAmount = 2048;
            this.dataManager = manager;
        }

        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag slots = new CompoundTag();
            for (int i = 0; i < definitionList.size(); i++) {
                slots.put(i + "", definitionList.get(i).serializeNBT(provider));
            }
            CompoundTag output = new CompoundTag();
            output.put("Slots", slots);
            output.putInt("MaxAmount", this.maxAmount);
            return output;
        }

        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            CompoundTag slots = nbt.getCompoundOrEmpty("Slots");
            for (String s : slots.keySet()) {
                definitionList.get(Integer.parseInt(s)).deserializeNBT(provider, slots.getCompoundOrEmpty(s));
            }
            this.maxAmount = nbt.getIntOr("MaxAmount", 0);
        }

        @Override
        public int getSlots() {
            return definitionList.size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return definitionList.get(slot).getStack().copyWithCount(definitionList.get(slot).getAmount());
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (isItemValid(slot, stack)) {
                SlotDefinition definition = definitionList.get(slot);
                int inserted = Math.min(maxAmount - definition.getAmount(), stack.getCount());
                if (definition.isVoidItems()) inserted = stack.getCount(); //Void
                if (!simulate) {
                    definition.setStack(stack);
                    definition.setAmount(Math.min(definition.getAmount() + inserted, maxAmount));
                    markDirty();
                }
                if (inserted == stack.getCount()) return ItemStack.EMPTY;
                return stack.copyWithCount(stack.getCount() - inserted);
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) return ItemStack.EMPTY;
            SlotDefinition definition = definitionList.get(slot);
            if (definition.getStack().isEmpty()) return ItemStack.EMPTY;
            if (definition.getAmount() <= amount) {
                ItemStack out = definition.getStack().copy();
                int newAmount = definition.getAmount();
                if (!simulate) {
                    definition.setAmount(0);
                    markDirty();
                }
                out.setCount(newAmount);
                return out;
            } else {
                if (!simulate) {
                    definition.setAmount(definition.getAmount() - amount);
                    markDirty();
                }
                return definition.getStack().copyWithCount(amount);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return maxAmount;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            SlotDefinition def = definitionList.get(slot);
            return def.getStack().isEmpty() || ItemStack.isSameItemSameComponents(def.getStack(), stack);
        }

        public void setMaxAmount(int maxAmount) {
            this.maxAmount = maxAmount;
            markDirty();
        }

        public void markDirty() {
            if (dataManager != null) dataManager.setDirty();
        }

        public SlotDefinition getSlotDefinition(int slot) {
            return definitionList.get(slot);
        }
    }

    public static class SlotDefinition {

        private ItemStack stack;
        private int amount;
        private boolean voidItems;
        private boolean refillItems;
        private BackpackItemHandler parent;

        public SlotDefinition(BackpackItemHandler parent) {
            this.stack = ItemStack.EMPTY;
            this.amount = 0;
            this.voidItems = true;
            this.refillItems = false;
            this.parent = parent;
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
            parent.markDirty();
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
            parent.markDirty();
        }

        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.put("Stack", stack.saveOptional(provider));
            compoundNBT.putInt("Amount", amount);
            compoundNBT.putBoolean("Void", voidItems);
            compoundNBT.putBoolean("Refill", refillItems);
            return compoundNBT;
        }

        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            this.stack = ItemStack.parseOptional(provider, nbt.getCompoundOrEmpty("Stack"));
            this.amount = nbt.getIntOr("Amount", 0);
            this.voidItems = nbt.getBooleanOr("Void", false);
            this.refillItems = nbt.getBooleanOr("Refill", false);
        }

        public boolean isVoidItems() {
            return voidItems;
        }

        public void setVoidItems(boolean voidItems) {
            this.voidItems = voidItems;
            parent.markDirty();
        }

        public boolean isRefillItems() {
            return refillItems;
        }

        public void setRefillItems(boolean refillItems) {
            this.refillItems = refillItems;
            parent.markDirty();
        }
    }
}
