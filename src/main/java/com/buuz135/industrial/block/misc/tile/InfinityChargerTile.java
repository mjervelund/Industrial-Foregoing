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
package com.buuz135.industrial.block.misc.tile;

import com.buuz135.industrial.block.tile.IndustrialMachineTile;
import com.buuz135.industrial.item.infinity.InfinityEnergyStorage;
import com.buuz135.industrial.module.ModuleMisc;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class InfinityChargerTile extends IndustrialMachineTile<InfinityChargerTile> {

    @Save
    private SidedInventoryComponent<InfinityChargerTile> chargingSlot;

    public InfinityChargerTile(BlockPos blockPos, BlockState blockState) {
        super(ModuleMisc.INFINITY_CHARGER, blockPos, blockState);
        addInventory(chargingSlot = (SidedInventoryComponent<InfinityChargerTile>) new SidedInventoryComponent<InfinityChargerTile>("charging", 80, 40, 1, 0)
                .setColor(DyeColor.BLUE)
                .setSlotLimit(1)
                .setInputFilter((stack, integer) -> !stack.isEmpty() && ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM) != null)
        );
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, InfinityChargerTile blockEntity) {
        if (!chargingSlot.getStackInSlot(0).isEmpty() && this.shouldWork()) {
            var itemEnergy = ItemAccess.forHandlerIndex(chargingSlot, 0).getCapability(Capabilities.Energy.ITEM);
            if (itemEnergy != null && this.getEnergyStorage() instanceof InfinityEnergyStorage<?> storage) {
                if (itemEnergy instanceof InfinityEnergyStorage<?> infinityStorage) {
                    long added = Math.min(Long.MAX_VALUE - infinityStorage.getLongEnergyStored(), Math.min(storage.getLongCapacity(), storage.getLongEnergyStored()));
                    infinityStorage.setEnergyStored(infinityStorage.getLongEnergyStored() + added);
                    storage.setEnergyStored(storage.getLongEnergyStored() - added);
                    markForUpdate();
                } else {
                    int inserted;
                    try (var transaction = Transaction.open(null)) {
                        inserted = itemEnergy.insert(storage.getAmountAsInt(), transaction);
                        transaction.commit();
                    }
                    storage.setEnergyStored(storage.getLongEnergyStored() - inserted);
                    markForUpdate();
                }
            }
            this.getRedstoneManager().finish();
        }
    }

    @Override
    public InfinityChargerTile getSelf() {
        return this;
    }

    @Override
    protected EnergyStorageComponent<InfinityChargerTile> createEnergyStorage() {
        return new InfinityEnergyStorage<>(1_000_000_000_000L, 10, 20);
    }
}
