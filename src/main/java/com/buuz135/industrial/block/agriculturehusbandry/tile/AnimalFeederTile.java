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
package com.buuz135.industrial.block.agriculturehusbandry.tile;

import com.buuz135.industrial.utils.FuelUtils;
import com.buuz135.industrial.utils.ItemStackUtils;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.tile.RangeManager;
import com.buuz135.industrial.config.machine.agriculturehusbandry.AnimalFeederConfig;
import com.buuz135.industrial.module.ModuleAgricultureHusbandry;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import javax.annotation.Nonnull;
import java.util.List;

public class AnimalFeederTile extends IndustrialAreaWorkingTile<AnimalFeederTile> {

    private int maxProgress;
    private int powerPerOperation;

    @Save
    private SidedInventoryComponent<AnimalFeederTile> input;

    public AnimalFeederTile(BlockPos blockPos, BlockState blockState) {
        super(ModuleAgricultureHusbandry.ANIMAL_FEEDER, RangeManager.RangeType.BEHIND, true, AnimalFeederConfig.powerPerOperation, blockPos, blockState);
        addInventory(input = (SidedInventoryComponent<AnimalFeederTile>) new SidedInventoryComponent<AnimalFeederTile>("food", 53, 22, 6 * 3, 0)
                .setColor(DyeColor.BLUE)
                // food stays in, but the remainders fed animals leave (water buckets ...) can be pulled out
                .setOutputFilter((stack, integer) -> isRemainder(stack))
                .setRange(6, 3)
                .setComponentHarness(this)
        );
        this.maxProgress = AnimalFeederConfig.maxProgress;
        this.powerPerOperation = AnimalFeederConfig.powerPerOperation;
    }

    @Override
    public WorkAction work() {
        if (hasEnergy(powerPerOperation)) {
            List<Animal> mobs = this.level.getEntitiesOfClass(Animal.class, getWorkingArea().bounds());
            if (mobs.size() == 0 || mobs.size() > AnimalFeederConfig.maxAnimalInTheArea) return new WorkAction(1, 0);
            mobs.removeIf(animalEntity -> animalEntity.getAge() != 0 || !animalEntity.canFallInLove() || getFeedingSlot(animalEntity) < 0);
            for (Animal firstParent : mobs) {
                for (Animal secondParent : mobs) {
                    if (firstParent.equals(secondParent) || !firstParent.getClass().equals(secondParent.getClass()))
                        continue;
                    int firstSlot = getFeedingSlot(firstParent);
                    ItemStack original = input.getStackInSlot(firstSlot).copyWithCount(1);
                    ItemStackUtils.shrink(input, firstSlot, 1);
                    int secondSlot = getFeedingSlot(secondParent);
                    if (secondSlot < 0) {
                        ItemUtil.insertItemReturnRemaining(input, original, false, null);
                        continue;
                    }
                    ItemStack secondFood = input.getStackInSlot(secondSlot).copyWithCount(1);
                    ItemStackUtils.shrink(input, secondSlot, 1);
                    giveBackRemainder(original);
                    giveBackRemainder(secondFood);
                    firstParent.setInLove(null);
                    secondParent.setInLove(null);
                    return new WorkAction(0.5f, powerPerOperation);
                }
            }
        }
        return new WorkAction(1, 0);
    }

    /**
     * What a player gets back when feeding this item (Axolotl#usePlayerItem: tropical fish bucket -> water bucket;
     * otherwise the crafting remainder). Put back into the food slots, dropped on top of the feeder if they are full.
     */
    private void giveBackRemainder(ItemStack food) {
        ItemStack remainder = food.is(Items.TROPICAL_FISH_BUCKET) ? new ItemStack(Items.WATER_BUCKET) : FuelUtils.getCraftingRemainder(food);
        if (remainder.isEmpty()) return;
        ItemStack left = ItemUtil.insertItemReturnRemaining(input, remainder, false, null);
        if (!left.isEmpty()) Block.popResource(this.level, this.worldPosition.above(), left);
    }

    private static boolean isRemainder(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET) || stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE);
    }

    private int getFeedingSlot(Animal entity) {
        for (int i = 0; i < input.getSlots(); i++) {
            if (entity.isFood(input.getStackInSlot(i))) return i;
        }
        return -1;
    }

    @Override
    protected EnergyStorageComponent<AnimalFeederTile> createEnergyStorage() {
        return new EnergyStorageComponent<>(AnimalFeederConfig.maxStoredPower, 10, 20);
    }

    @Override
    public int getMaxProgress() {
        return maxProgress;
    }


    @Nonnull
    @Override
    public AnimalFeederTile getSelf() {
        return this;
    }
}
