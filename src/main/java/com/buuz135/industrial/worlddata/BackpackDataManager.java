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

import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.TransferUtils;
import com.hrznstudio.titanium.util.ItemStackSerialization;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BackpackDataManager extends SavedData {
    public static HashMap<String, BackpackItemHandler> CLIENT_SIDE_BACKPACKS = new HashMap<>();

    public static final String NAME = "IFBackpack";
    public static final int SLOT_AMOUNT = 4 * 8;
    // The "data" tag keeps the 1.21 layout (Backpacks/<id>/Slots, MaxAmount); only the file moved, see LegacySavedData
    public static final SavedDataType<BackpackDataManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "backpack"),
            BackpackDataManager::create,
            level -> CompoundTag.CODEC.xmap(tag -> load(registries(level), tag), manager -> manager.save(new CompoundTag(), registries(level))));

    public HashMap<String, BackpackItemHandler> itemHandlers;

    public BackpackDataManager(String name) {
        itemHandlers = new HashMap<>();
    }

    public BackpackDataManager() {
        itemHandlers = new HashMap<>();
    }

    private static BackpackDataManager create(@Nullable ServerLevel level) {
        CompoundTag legacy = LegacySavedData.read(level, NAME);
        if (legacy == null) return new BackpackDataManager();
        BackpackDataManager manager = load(registries(level), legacy);
        manager.setDirty();
        return manager;
    }

    private static HolderLookup.Provider registries(@Nullable ServerLevel level) {
        return Objects.requireNonNull(level, "IF backpack data is level storage").registryAccess();
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
            return serverWorld.getDataStorage().computeIfAbsent(TYPE);
        }
        return null;
    }

    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT(provider)));
        compound.put("Backpacks", nbt);
        return compound;
    }

    public static class BackpackItemHandler implements ResourceHandler<ItemResource> {

        private List<SlotDefinition> definitionList;
        private List<SlotJournal> journals;
        private int maxAmount;
        private BackpackDataManager dataManager;

        public BackpackItemHandler(BackpackDataManager manager) {
            definitionList = new ArrayList<>();
            journals = new ArrayList<>();
            for (int i = 0; i < SLOT_AMOUNT; i++) {
                definitionList.add(new SlotDefinition(this));
                journals.add(new SlotJournal(definitionList.get(i)));
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
        public int size() {
            return definitionList.size();
        }

        @Override
        public ItemResource getResource(int index) {
            SlotDefinition definition = definitionList.get(index);
            return definition.amount > 0 ? ItemResource.of(definition.stack) : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            SlotDefinition definition = definitionList.get(index);
            return definition.stack.isEmpty() ? 0 : definition.amount;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return resource.isEmpty() || isValid(index, resource) ? maxAmount : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            SlotDefinition def = definitionList.get(index);
            return def.stack.isEmpty() || resource.matches(def.stack);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!isValid(index, resource)) return 0;
            SlotDefinition definition = definitionList.get(index);
            int inserted = Math.max(0, Math.min(maxAmount - definition.amount, amount));
            if (definition.voidItems) inserted = amount; //Void
            if (inserted > 0) {
                journals.get(index).updateSnapshots(transaction);
                definition.stack = resource.toStack(1);
                definition.amount = Math.min(definition.amount + inserted, maxAmount);
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            SlotDefinition definition = definitionList.get(index);
            if (definition.stack.isEmpty() || !resource.matches(definition.stack)) return 0;
            int extracted = Math.min(amount, definition.amount);
            if (extracted > 0) {
                journals.get(index).updateSnapshots(transaction);
                definition.amount -= extracted;
            }
            return extracted;
        }

        public int getSlots() {
            return size();
        }

        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return definitionList.get(slot).getStack().copyWithCount(definitionList.get(slot).getAmount());
        }

        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            try (var transaction = Transaction.open(null)) {
                int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
                if (!simulate) transaction.commit();
                if (inserted == stack.getCount()) return ItemStack.EMPTY;
                return stack.copyWithCount(stack.getCount() - inserted);
            }
        }

        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return TransferUtils.extractItem(this, slot, amount, simulate);
        }

        public int getSlotLimit(int slot) {
            return maxAmount;
        }

        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return isValid(slot, ItemResource.of(stack));
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

        private record SlotSnapshot(ItemStack stack, int amount) {
        }

        private class SlotJournal extends SnapshotJournal<SlotSnapshot> {
            private final SlotDefinition definition;

            private SlotJournal(SlotDefinition definition) {
                this.definition = definition;
            }

            @Override
            protected SlotSnapshot createSnapshot() {
                return new SlotSnapshot(definition.stack, definition.amount);
            }

            @Override
            protected void revertToSnapshot(SlotSnapshot snapshot) {
                definition.stack = snapshot.stack();
                definition.amount = snapshot.amount();
            }

            @Override
            protected void onRootCommit(SlotSnapshot originalState) {
                markDirty();
            }
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
            compoundNBT.put("Stack", ItemStackSerialization.save(provider, stack));
            compoundNBT.putInt("Amount", amount);
            compoundNBT.putBoolean("Void", voidItems);
            compoundNBT.putBoolean("Refill", refillItems);
            return compoundNBT;
        }

        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            this.stack = ItemStackSerialization.load(provider, nbt.getCompoundOrEmpty("Stack"));
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
