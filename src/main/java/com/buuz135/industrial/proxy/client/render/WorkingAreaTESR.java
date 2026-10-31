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

import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public class WorkingAreaTESR implements BlockEntityRenderer<IndustrialAreaWorkingTile, WorkingAreaTESR.State> {

    public WorkingAreaTESR(BlockEntityRendererProvider.Context p_173540_) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IndustrialAreaWorkingTile tileEntityIn, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tileEntityIn, state, partialTicks, cameraPosition, breakProgress);
        state.area = null;
        if (!tileEntityIn.isShowingArea()) return;
        BlockPos blockpos = tileEntityIn.getBlockPos();
        state.area = tileEntityIn.getWorkingArea().bounds().inflate(0.001).move(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
        Color color = new Color(Math.abs(blockpos.getX() % 255), Math.abs(blockpos.getY() % 255), Math.abs(blockpos.getZ() % 255));
        state.red = color.getRed() / 255f;
        state.green = color.getGreen() / 255f;
        state.blue = color.getBlue() / 255f;
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.area == null) return;
        float width = Math.max(2.5F, (float) Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F);
        collector.submitShapeOutline(stack, Shapes.create(state.area), RenderTypes.linesTranslucent(), ARGB.colorFromFloat(0.5F, state.red, state.green, state.blue), width, false);
        AABB area = state.area;
        float red = state.red, green = state.green, blue = state.blue;
        collector.submitCustomGeometry(stack, RenderTypes.debugQuads(), (pose, buffer) -> renderFaces(pose.pose(), buffer, area, red, green, blue, 0.3F));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private static void renderFaces(Matrix4f matrix, VertexConsumer buffer, AABB pos, float red, float green, float blue, float alpha) {

        float x1 = (float) pos.minX;
        float x2 = (float) pos.maxX;
        float y1 = (float) pos.minY;
        float y2 = (float) pos.maxY;
        float z1 = (float) pos.minZ;
        float z2 = (float) pos.maxZ;

        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);


        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);

    }

    @Override
    public AABB getRenderBoundingBox(IndustrialAreaWorkingTile blockEntity) {
        return blockEntity.getWorkingArea().bounds();
    }

    public static class State extends BlockEntityRenderState {
        public @Nullable AABB area;
        public float red;
        public float green;
        public float blue;
    }
}
