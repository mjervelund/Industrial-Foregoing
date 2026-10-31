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

package com.buuz135.industrial.proxy.client.model;

import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.buuz135.industrial.module.ModuleTransportStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Adds the standalone models of the installed conveyor upgrades (see ModuleTransportStorage) to the block model.
 */
public class ConveyorBlockModel extends DelegateBlockStateModel {

    public ConveyorBlockModel(BlockStateModel previousConveyor) {
        super(previousConveyor);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        super.collectParts(level, pos, state, random, parts);
        ModelData extraData = level.getModelData(pos);
        if (extraData.has(ConveyorModelData.UPGRADE_PROPERTY)) {
            for (ConveyorUpgrade upgrade : extraData.get(ConveyorModelData.UPGRADE_PROPERTY).getUpgrades().values()) {
                if (upgrade == null)
                    continue;
                BlockStateModelPart part = Minecraft.getInstance().getModelManager().getStandaloneModel(ModuleTransportStorage.CONVEYOR_UPGRADES_CACHE.get(upgrade.getFactory().getModel(upgrade.getSide(), state.getValue(ConveyorBlock.FACING))));
                if (part != null) parts.add(part);
            }
        }
    }

}
