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

package com.buuz135.industrial.entity.client;

import com.buuz135.industrial.entity.InfinityLauncherProjectileEntity;
import com.buuz135.industrial.utils.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class InfinityLauncherProjectileRenderer extends EntityRenderer<InfinityLauncherProjectileEntity, InfinityLauncherProjectileRenderer.State> {

    public static final ModelLayerLocation PROJECTILE_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_launcher_projectile"), "main");
    public static final Identifier PROJECTILE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/item/infinity_launcher_projectile.png");
    private final InfinityLauncherProjectileModel projectileModel;

    public InfinityLauncherProjectileRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        projectileModel = new InfinityLauncherProjectileModel(p_174008_.bakeLayer(PROJECTILE_LAYER));
    }

    @Override
    public void submit(State state, PoseStack matrixStackIn, SubmitNodeCollector collector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        matrixStackIn.rotateDegrees(Axis.ZP, state.xRot);
        collector.submitModel(this.projectileModel, Unit.INSTANCE, matrixStackIn, PROJECTILE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrixStackIn.popPose();
        super.submit(state, matrixStackIn, collector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfinityLauncherProjectileEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
    }

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
    }
}
