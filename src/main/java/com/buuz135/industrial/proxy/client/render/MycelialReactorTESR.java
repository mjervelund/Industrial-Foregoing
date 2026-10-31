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

import com.buuz135.industrial.block.generator.tile.MycelialReactorTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public class MycelialReactorTESR implements BlockEntityRenderer<MycelialReactorTile, MycelialReactorTESR.State> {

    // 1.21 used a custom SRC_ALPHA/SRC_ALPHA blend (additive at full alpha) without depth writes; energy swirl is additive and emissive
    public static RenderType TYPE = RenderTypes.energySwirl(Identifier.fromNamespaceAndPath("industrialforegoing", "textures/block/mycelial.png"), 0, 0);

    public MycelialReactorTESR(BlockEntityRendererProvider.Context p_173540_) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MycelialReactorTile te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);
        state.active = te.getBar().getProgress() != 0;
        state.gameTime = te.getLevel() == null ? 0 : te.getLevel().getGameTime();
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.active) return;
        stack.pushPose();
        stack.scale(1.5F, 1.5F, 1.5F);
        stack.translate(0.30, 1.75, 0.3D);
        stack.rotate(camera.orientation);
        stack.rotate(Axis.ZP.rotationDegrees((state.gameTime / 20F) % 360));
        stack.rotate(Axis.XP.rotationDegrees(-90f));
        long gameTime = state.gameTime;
        collector.submitCustomGeometry(stack, TYPE, (pose, buffer) -> renderQuads(pose.pose(), buffer, gameTime));
        stack.popPose();
    }

    private static void renderQuads(Matrix4f matrix, VertexConsumer buffer1, long gameTime) {
        double speed = 20;
        //if (true) speed = 20;
        float sin = ((float) Math.sin(gameTime / speed)) * 0.05f;
        float pX1 = 1;
        float u = 1;
        float pX2 = 0;
        float u2 = 0;
        int color = ARGB.opaque(Color.CYAN.getRGB());
        float xOffset = -0.75f;
        float yOffset = -0f;
        float zOffset = -0.75f;
        vertex(buffer1, matrix, pX2 + xOffset + sin, yOffset, 0 + zOffset + sin, u2, 0, color);
        vertex(buffer1, matrix, pX1 + xOffset - sin + 0.5f, yOffset, 0 + zOffset + sin, u, 0, color);
        vertex(buffer1, matrix, pX1 + xOffset - sin + 0.5f, yOffset, 1.5f + zOffset - sin, u, 1, color);
        vertex(buffer1, matrix, pX2 + xOffset + sin, yOffset, 1.5f + zOffset - sin, u2, 1, color);
        yOffset = 0.01f;
        sin = ((float) Math.cos(gameTime / speed)) * 0.05f;
        color = ARGB.opaque(0xB578FF);
        vertex(buffer1, matrix, pX2 + xOffset + sin, yOffset, 0 + zOffset + sin, u2, 0, color);
        vertex(buffer1, matrix, pX1 + xOffset - sin + 0.5f, yOffset, 0 + zOffset - sin, u, 0, color);
        vertex(buffer1, matrix, pX1 + xOffset + sin + 0.5f, yOffset, 1.5f + zOffset + sin, u, 1, color);
        vertex(buffer1, matrix, pX2 + xOffset - sin, yOffset, 1.5f + zOffset + sin, u2, 1, color);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(matrix, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox(MycelialReactorTile blockEntity) {
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity).inflate(4);
    }

    public static class State extends BlockEntityRenderState {
        public boolean active;
        public long gameTime;
    }
}
