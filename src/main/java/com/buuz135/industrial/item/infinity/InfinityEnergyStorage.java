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

package com.buuz135.industrial.item.infinity;

import com.buuz135.industrial.gui.component.InfinityEnergyScreenAddon;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Long-based energy storage, receive only. The int fields of {@link EnergyStorageComponent} are unused: every read goes
 * through {@link #getLongEnergyStored()} and every write through {@link #setEnergyStored(long, TransactionContext)}, so
 * item-backed subclasses can store the energy in a data component through their {@link net.neoforged.neoforge.transfer.access.ItemAccess}.
 */
public class InfinityEnergyStorage<T extends IComponentHarness> extends EnergyStorageComponent<T> {

    private final long capacity;
    private long energy;
    private final EnergyJournal journal = new EnergyJournal();

    public InfinityEnergyStorage(long capacity, int xPos, int yPos) {
        super(Integer.MAX_VALUE, xPos, yPos);
        this.energy = 0;
        this.capacity = capacity;
    }

    @Override
    public long getAmountAsLong() {
        return getLongEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return getLongCapacity();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (!canReceive()) return 0;
        long stored = getLongEnergyStored();
        int energyReceived = (int) Math.max(0, Math.min(getLongCapacity() - stored, amount));
        if (energyReceived > 0 && setEnergyStored(stored + energyReceived, transaction))
            return energyReceived;
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    /**
     * Writes the stored energy as part of a transaction, returns false if it could not be written.
     */
    protected boolean setEnergyStored(long power, TransactionContext transaction) {
        journal.updateSnapshots(transaction);
        this.energy = power;
        return true;
    }

    /**
     * Writes the stored energy immediately, outside of any transaction.
     */
    public void setEnergyStored(long power) {
        try (var transaction = Transaction.open(null)) {
            setEnergyStored(power, transaction);
            transaction.commit();
        }
    }

    @Override
    public void setEnergyStored(int energy) {
        setEnergyStored((long) energy);
    }

    public boolean canExtract() {
        return false;
    }

    public boolean canReceive() {
        return true;
    }

    public long getLongEnergyStored() {
        return this.energy;
    }

    public long getLongCapacity() {
        return capacity;
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Collections.emptyList();
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("energy", this.energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.energy = input.getLongOr("energy", 0L);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<IFactory<?>> getScreenAddons() {
        return Collections.singletonList(() -> new InfinityEnergyScreenAddon(getX(), getY(), this));
    }

    private class EnergyJournal extends SnapshotJournal<Long> {
        @Override
        protected Long createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            energy = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            if (energy != originalState && componentHarness != null) {
                componentHarness.markComponentForUpdate(false);
            }
        }
    }
}
