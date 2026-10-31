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

package com.buuz135.industrial.proxy.client.render;

import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class FluidConveyorTESR implements BlockEntityRenderer<ConveyorTile, FluidConveyorTESR.State> {

    public FluidConveyorTESR(BlockEntityRendererProvider.Context p_173540_) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ConveyorTile te, State state, float partialTicks, net.minecraft.world.phys.Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);
        state.still = null;
        if (te.getTank().getFluidAmount() <= 0 || te.getLevel() == null) return;
        FluidStack fluid = te.getTank().getFluid();
        // Fluid sprites and tints live in the client fluid models on 26.3; the block atlas animates the sprites
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
        state.flow = model.flowingMaterial().sprite();
        state.still = model.stillMaterial().sprite();
        state.color = model.fluidTintSource() == null ? 0xFFFFFFFF : model.fluidTintSource().colorAsStack(fluid);
        state.facing = te.getFacing();
        state.renderPrev = !(te.getLevel().getBlockEntity(te.getBlockPos().relative(state.facing.getOpposite())) instanceof ConveyorTile prev) || prev.getTank().getFluidAmount() <= 0;
        state.renderNext = !(te.getLevel().getBlockEntity(te.getBlockPos().relative(state.facing)) instanceof ConveyorTile next) || next.getTank().getFluidAmount() <= 0;
    }

    @Override
    public void submit(State state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.still == null) return;
        matrixStack.pushPose();
        Direction facing = state.facing;
        if (facing == Direction.NORTH) {
            matrixStack.translate(1, 0, 1);
            matrixStack.rotate(Axis.YP.rotationDegrees(180));
        }
        if (facing == Direction.EAST) {
            matrixStack.translate(0, 0, 1);
            matrixStack.rotate(Axis.YP.rotationDegrees(90));
        }
        if (facing == Direction.WEST) {
            matrixStack.translate(1, 0, 0);
            matrixStack.rotate(Axis.YN.rotationDegrees(90));
        }
        collector.submitCustomGeometry(matrixStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> renderFluid(pose.pose(), buffer, state));
        matrixStack.popPose();
    }

    private static void renderFluid(Matrix4f matrix, VertexConsumer buffer, State state) {
        float posY = 2 / 16f - 1 / 32f;
        float right = 1 / 16f;
        float left = 15 / 16f;
        //ConveyorBlock.EnumSides sides = te.getWorld().getBlockState(te.getPos()).getBlock().getExtendedState(te.getWorld().getBlockState(te.getPos()), te.getWorld(), te.getPos()).get(ConveyorBlock.SIDES);
        ConveyorBlock.EnumSides sides = ConveyorBlock.EnumSides.NONE;
        if (sides == ConveyorBlock.EnumSides.BOTH || sides == ConveyorBlock.EnumSides.RIGHT) right = 0;
        if (sides == ConveyorBlock.EnumSides.BOTH || sides == ConveyorBlock.EnumSides.LEFT) left = 1;
        int color = state.color;
        int light = state.lightCoords;
        TextureAtlasSprite flow = state.flow;
        TextureAtlasSprite still = state.still;

        vertex(buffer, matrix, left, posY, 0, color, flow.getU(0), flow.getV(0), light);
        vertex(buffer, matrix, right, posY, 0, color, flow.getU(0.5f), flow.getV(0), light);
        vertex(buffer, matrix, right, posY, 1, color, flow.getU(0.5f), flow.getV(1), light);
        vertex(buffer, matrix, left, posY, 1, color, flow.getU(0), flow.getV(1), light);

        // One pixel row of the still texture as the fluid's front/back edge
        float edge = 1 - 1f / still.contents().height();
        if (state.renderPrev) {
            vertex(buffer, matrix, right, posY, 0, color, still.getU(0), still.getV(edge), light);
            vertex(buffer, matrix, left, posY, 0, color, still.getU(1), still.getV(edge), light);
            vertex(buffer, matrix, left, 1 / 16f, 0, color, still.getU(1), still.getV(1), light);
            vertex(buffer, matrix, right, 1 / 16f, 0, color, still.getU(0), still.getV(1), light);
        }
        if (state.renderNext) {
            vertex(buffer, matrix, left, posY, 1, color, still.getU(1), still.getV(edge), light);
            vertex(buffer, matrix, right, posY, 1, color, still.getU(0), still.getV(edge), light);
            vertex(buffer, matrix, right, 1 / 16f, 1, color, still.getU(0), still.getV(1), light);
            vertex(buffer, matrix, left, 1 / 16f, 1, color, still.getU(1), still.getV(1), light);
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int color, float u, float v, int light) {
        buffer.addVertex(matrix, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }

    public static class State extends BlockEntityRenderState {
        public @Nullable TextureAtlasSprite still;
        public TextureAtlasSprite flow;
        public int color;
        public Direction facing = Direction.NORTH;
        public boolean renderPrev;
        public boolean renderNext;
    }
}
