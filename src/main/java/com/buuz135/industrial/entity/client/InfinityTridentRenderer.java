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

import com.buuz135.industrial.entity.InfinityTridentEntity;
import com.buuz135.industrial.utils.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class InfinityTridentRenderer extends EntityRenderer<InfinityTridentEntity, InfinityTridentRenderer.State> {

    public static final ModelLayerLocation TRIDENT_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_trident"), "main");
    public static final Identifier TRIDENT = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/item/infinity_trident.png");
    private final InfinityTridentModel tridentModel;

    public InfinityTridentRenderer(EntityRendererProvider.Context p_174286_) {
        super(p_174286_);
        tridentModel = new InfinityTridentModel(p_174286_.bakeLayer(TRIDENT_LAYER));
    }

    @Override
    public void submit(State state, PoseStack matrixStackIn, SubmitNodeCollector collector, CameraRenderState camera) {
        matrixStackIn.pushPose();
        matrixStackIn.rotateDegrees(Axis.YP, state.yRot - 90.0F);
        matrixStackIn.rotateDegrees(Axis.ZP, state.xRot + 90.0F);
        matrixStackIn.translate(0.5, -0.5, -0.5);
        collector.submitModel(this.tridentModel, Unit.INSTANCE, matrixStackIn, state.isFoil ? RenderTypes.itemTranslucentGlint(TRIDENT) : RenderTypes.entityTranslucent(TRIDENT), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrixStackIn.popPose();
        super.submit(state, matrixStackIn, collector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfinityTridentEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.isFoil = entity.isEnchanted();
    }

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public boolean isFoil;
    }
}
