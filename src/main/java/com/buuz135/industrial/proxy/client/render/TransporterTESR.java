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

import com.buuz135.industrial.api.transporter.TransporterType;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.block.transportstorage.transporter.TransporterItemType;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public class TransporterTESR implements BlockEntityRenderer<TransporterTile, TransporterTESR.State> {

    public static Identifier TEXTURE = Identifier.fromNamespaceAndPath("industrialforegoing", "textures/block/transporters/particle.png");

    public static RenderType TYPE = createRenderType();

    public TransporterTESR(BlockEntityRendererProvider.Context context) {
    }

    public static RenderType createRenderType() {
        // 1.21: position/tex/colour quads with ONE/ONE blending; energy swirl is the vanilla additive textured type
        return RenderTypes.energySwirl(TEXTURE, 0, 0);
    }

    /**
     * The camera-facing particle quad the transporters draw for an empty queue slot or a fluid packet.
     */
    public static void submitParticle(PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera, int color, float scale) {
        stack.pushPose();
        stack.rotate(camera.orientation);
        stack.rotate(Axis.XP.rotationDegrees(-90f));
        stack.scale(scale, scale, scale);
        collector.submitCustomGeometry(stack, TYPE, (pose, buffer) -> {
            Matrix4f matrix = pose.pose();
            float pX1 = 1;
            float u = 1;
            float pX2 = 0;
            float u2 = 0;
            float xOffset = -0.75f;
            float yOffset = -0f;
            float zOffset = -0.75f;
            particleVertex(buffer, matrix, pX2 + xOffset, yOffset, 0 + zOffset, u2, 0, color);
            particleVertex(buffer, matrix, pX1 + xOffset + 0.5f, yOffset, 0 + zOffset, u, 0, color);
            particleVertex(buffer, matrix, pX1 + xOffset + 0.5f, yOffset, 1.5f + zOffset, u, 1, color);
            particleVertex(buffer, matrix, pX2 + xOffset, yOffset, 1.5f + zOffset, u2, 1, color);
        });
        stack.popPose();
    }

    private static void particleVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(matrix, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
    }

    public static Vector3f getPath(Direction from, Direction to, double step, float partialTicks) {
        float totalSteps = 5f;
        if (from.getOpposite() == to) {
            totalSteps = 7f;
            Vec3 vector3d = new Vec3(to.step().x() / totalSteps * step, to.step().y() / totalSteps * step, to.step().z() / totalSteps * step);
            if (from.getAxis() == Direction.Axis.X) {
                vector3d = vector3d.add(0, 0.5, 0.5);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(1, 0, 0);
                }
            }
            if (from.getAxis() == Direction.Axis.Z) {
                vector3d = vector3d.add(0.5, 0.5, 0);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(0, 0, 1);
                }
            }
            if (from.getAxis() == Direction.Axis.Y) {
                vector3d = vector3d.add(0.5, 0, 0.5);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(0, 1, 0);
                }
            }
            float divideSecond = -0.22f;
            vector3d = vector3d.add(from.step().x() * divideSecond, from.step().y() * divideSecond, from.step().z() * divideSecond);
            return new Vector3f((float) vector3d.x, (float) vector3d.y, (float) vector3d.z);
        }
        Vector3f vsrc = from.step();
        Vector3f vdst = to.step();
        Vector3f a = new Vector3f(vsrc);
        a.mul(5 / 16f);
        Vector3f b = new Vector3f(vdst);
        b.mul(6 / 16f);
        Vector3f c = new Vector3f(vsrc);
        c.mul(3 / 16f);
        float sind = -Mth.sin((float) ((step / totalSteps) * Math.PI / 2f));
        float cosd = -Mth.cos((float) ((step / totalSteps) * Math.PI / 2f));
        a.mul(sind);
        b.mul(cosd);
        a.add(b);
        a.add(c);
        float divide = 2.5f;
        a.add(vdst.x() / divide, vdst.y() / divide, vdst.z() / divide);
        a.add(0.5f, 0.5f, 0.5f);
        float divideSecond = 0.15f;
        a.add(vsrc.x() * divideSecond, vsrc.y() * divideSecond, vsrc.z() * divideSecond);
        return a;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TransporterTile tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        // The transfer queues live in the client tile; submit runs in the same frame on the render thread
        state.tile = tile;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        TransporterTile tile = state.tile;
        if (tile == null) return;
        float partialTicks = state.partialTicks;
        Map<Direction, TransporterType> transporters = tile.getTransporterTypeMap();
        for (Direction direction : transporters.keySet()) {
            if (transporters.get(direction).getAction() == TransporterTypeFactory.TransporterAction.EXTRACT) {
                for (Direction other : transporters.keySet()) {
                    if (direction == other || !transporters.get(direction).getFactory().getName().equals(transporters.get(other).getFactory().getName()) || transporters.get(other).getAction() == TransporterTypeFactory.TransporterAction.EXTRACT)
                        continue;
                    for (int i = -1; i < TransporterItemType.QUEUE_SIZE; ++i) {
                        stack.pushPose();
                        Vector3f pos = getPath(direction, other, i + partialTicks, partialTicks);
                        stack.translate(pos.x(), pos.y(), pos.z());
                        transporters.get(other).renderTransfer(pos, direction, i + 1, stack, collector, camera, partialTicks, tile.getLevel());
                        stack.popPose();
                    }
                }
            }
        }
    }

    public static class State extends BlockEntityRenderState {
        public @Nullable TransporterTile tile;
        public float partialTicks;
    }
}
