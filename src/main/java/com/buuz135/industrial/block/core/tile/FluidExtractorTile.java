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

package com.buuz135.industrial.block.core.tile;

import com.buuz135.industrial.utils.TankUtils;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.tile.RangeManager;
import com.buuz135.industrial.config.machine.core.FluidExtractorConfig;
import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.recipe.FluidExtractorRecipe;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.hrznstudio.titanium.component.fluid.SidedFluidTankComponent;
import com.hrznstudio.titanium.util.RecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FluidExtractorTile extends IndustrialAreaWorkingTile<FluidExtractorTile> {

    public static HashMap<ResourceKey<Level>, HashMap<ChunkPos, HashMap<BlockPos, FluidExtractionProgress>>> EXTRACTION = new HashMap<>();

    private int maxProgress;
    private int powerPerOperation;
    private FluidExtractorRecipe currentRecipe;
    @Save
    private SidedFluidTankComponent<FluidExtractorTile> tank;

    public FluidExtractorTile(BlockPos blockPos, BlockState blockState) {
        super(ModuleCore.FLUID_EXTRACTOR, RangeManager.RangeType.BEHIND, false, FluidExtractorConfig.powerPerOperation, blockPos, blockState);
        addTank(tank = (SidedFluidTankComponent<FluidExtractorTile>) new SidedFluidTankComponent<FluidExtractorTile>("latex", FluidExtractorConfig.maxLatexTankSize, 43, 20, 0).
                setColor(DyeColor.LIGHT_GRAY).
                setTankAction(FluidTankComponent.Action.DRAIN).
                setComponentHarness(this)
        );
        this.maxProgress = FluidExtractorConfig.maxProgress;
        this.powerPerOperation = FluidExtractorConfig.powerPerOperation;
    }

    @Override
    public WorkAction work() {
        BlockPos pos = getPointedBlockPos();
        if (isLoaded(pos) && !this.level.isEmptyBlock(pos) && this.tank.getFluidAmount() < this.tank.getCapacity()) {
            if (currentRecipe == null || !currentRecipe.matches(this.level, pos) || currentRecipe.defaultRecipe)
                currentRecipe = findRecipe(this.level, pos);

            if (currentRecipe != null) {//GetDimensionType
                boolean powered = hasEnergy(powerPerOperation);

                HashMap<BlockPos, FluidExtractionProgress> chunkProgress = EXTRACTION.computeIfAbsent(this.level.dimension(), dimension -> new HashMap<>()).computeIfAbsent(this.level.getChunkAt(pos).getPos(), chunkPos -> new HashMap<>());
                FluidExtractionProgress extractionProgress = chunkProgress.get(pos);
                if (extractionProgress == null || !extractionProgress.matches(this.level, pos)) {
                    if (extractionProgress != null) extractionProgress.clear(this.level, pos);
                    extractionProgress = new FluidExtractionProgress(this.level, this.level.getBlockState(pos));
                    chunkProgress.put(pos, extractionProgress);
                }
                if (currentRecipe.outputsLatex()) {
                    TankUtils.fillForced(tank, currentRecipe.output.withAmount(currentRecipe.output.amount() * (powered ? 3 : 1)).create(), false);
                } else {
                    TankUtils.fillForced(tank, currentRecipe.getOutput(), false);
                }
                if (this.level.getRandom().nextDouble() <= currentRecipe.breakChance) {
                    extractionProgress.setProgress(extractionProgress.getProgress() + 1);
                }
                if (extractionProgress.getProgress() > 7) {
                    chunkProgress.remove(pos);
                    extractionProgress.clear(this.level, pos);
                    this.level.setBlockAndUpdate(pos, currentRecipe.result);
                    if (currentRecipe.outputsLatex()) {
                        TankUtils.fillForced(tank, currentRecipe.output.withAmount(currentRecipe.output.amount() * (powered ? 200 : 1)).create(), false);
                    }
                }
                if (powered)
                    return new WorkAction(0.4f, powerPerOperation);
                return new WorkAction(1f, 0);
            }
        }
        return new WorkAction(1, 0);
    }

    @Override
    protected EnergyStorageComponent<FluidExtractorTile> createEnergyStorage() {
        return new EnergyStorageComponent<>(FluidExtractorConfig.maxStoredPower, 10, 20);
    }

    @Override
    public int getMaxProgress() {
        return maxProgress;
    }

    @Nullable
    public FluidExtractorRecipe findRecipe(Level world, BlockPos pos) {
        Collection<FluidExtractorRecipe> recipeList = RecipeUtil.getRecipes(world, (RecipeType<FluidExtractorRecipe>) ModuleCore.FLUID_EXTRACTOR_TYPE.get());
        for (FluidExtractorRecipe recipe : recipeList) {
            if (!recipe.defaultRecipe && recipe.matches(world, pos)) return recipe;
        }
        for (FluidExtractorRecipe recipe : recipeList) {
            if (recipe.defaultRecipe && recipe.matches(world, pos)) return recipe;
        }
        return null;
    }

    @Nonnull
    @Override
    public FluidExtractorTile getSelf() {
        return this;
    }

    /**
     * Sends the crack overlay of every tracked extraction in this level, and drops the entries whose block is no
     * longer the one being extracted (log broken, replaced or stripped), clearing their overlay.
     */
    public static void tickProgress(Level level) {
        if (level.isClientSide()) return;
        HashMap<ChunkPos, HashMap<BlockPos, FluidExtractionProgress>> dimension = EXTRACTION.get(level.dimension());
        if (dimension == null) return;
        Iterator<HashMap<BlockPos, FluidExtractionProgress>> chunks = dimension.values().iterator();
        while (chunks.hasNext()) {
            Iterator<Map.Entry<BlockPos, FluidExtractionProgress>> entries = chunks.next().entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<BlockPos, FluidExtractionProgress> entry = entries.next();
                BlockPos pos = entry.getKey();
                FluidExtractionProgress progress = entry.getValue();
                if (!level.isLoaded(pos)) continue;
                if (!progress.matches(level, pos)) {
                    progress.clear(level, pos);
                    entries.remove();
                } else {
                    level.destroyBlockProgress(progress.getBreakID(), pos, progress.getProgress());
                }
            }
        }
        dimension.values().removeIf(HashMap::isEmpty);
    }

    public static class FluidExtractionProgress {

        private int progress;
        private int breakID;
        private final Block block;

        public FluidExtractionProgress(Level world, BlockState state) {
            this.progress = 0;
            this.breakID = world.getRandom().nextInt();
            this.block = state.getBlock();
        }

        /** Whether the block at {@code pos} is still the one this progress belongs to. */
        public boolean matches(Level level, BlockPos pos) {
            return level.getBlockState(pos).is(block);
        }

        /** Removes the crack overlay this progress put on {@code pos}. */
        public void clear(Level level, BlockPos pos) {
            level.destroyBlockProgress(breakID, pos, -1);
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getBreakID() {
            return breakID;
        }

    }
}
