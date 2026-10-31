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

package com.buuz135.industrial.proxy.client.event;

import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.item.ItemInfinityDrill;
import com.buuz135.industrial.module.ModuleTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class IFClientEvents {

    @SubscribeEvent
    public void blockOverlayEvent(ExtractBlockOutlineRenderStateEvent event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem().equals(ModuleTool.INFINITY_DRILL.get())) {
            BlockHitResult blockRayTraceResult = event.getHitResult();
            ItemStack hand = Minecraft.getInstance().player.getMainHandItem();
            InfinityTier tier = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getSelectedTier(hand);
            Level world = event.getLevel();
            Pair<BlockPos, BlockPos> area = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(), tier, false);
            // Extract the outlines here, the renderer must not touch the level
            List<Pair<BlockPos, VoxelShape>> shapes = new ArrayList<>();
            BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
                VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
                if (shape != null && !shape.isEmpty() && !world.isEmptyBlock(blockPos) && world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0 && !(world.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
                    shapes.add(Pair.of(blockPos.immutable(), shape));
                }
            });
            event.addCustomRenderer((renderState, collector, stack, levelRenderState) -> {
                Vec3 camera = levelRenderState.cameraRenderState.pos;
                float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
                for (Pair<BlockPos, VoxelShape> pair : shapes) {
                    BlockPos blockPos = pair.getLeft();
                    stack.pushPose();
                    stack.translate(blockPos.getX() - camera.x, blockPos.getY() - camera.y, blockPos.getZ() - camera.z);
                    collector.submitShapeOutline(stack, pair.getRight(), RenderTypes.linesTranslucent(), ARGB.colorFromFloat(0.35F, 0, 0, 0), width, false);
                    stack.popPose();
                }
                return true;
            });
        }
    }
}
