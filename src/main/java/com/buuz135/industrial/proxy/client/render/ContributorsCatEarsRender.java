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

import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.reward.storage.ClientRewardStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContributorsCatEarsRender extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final Identifier EARS_MODEL_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/catears");
    public static final StandaloneModelKey<BlockStateModelPart> EARS_MODEL = new StandaloneModelKey<>(EARS_MODEL_ID::toString);

    public ContributorsCatEarsRender(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float netHeadYaw, float headPitch) {
        // The render state carries no UUID; the rewards are keyed by player UUID
        Entity entity = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(state.id);
        if (entity == null) return;
        if (!ClientRewardStorage.REWARD_STORAGE.getRewards().containsKey(entity.getUUID())) return;
        if (!ClientRewardStorage.REWARD_STORAGE.getRewards().get(entity.getUUID()).getEnabled().containsKey(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "cat_ears")))
            return;
        stack.pushPose();
        stack.translate(0, -0.015f, 0);
        if (!state.headEquipment.isEmpty()) stack.translate(0, -0.02f, 0);
        if (state.isCrouching) stack.translate(0, 0.27f, 0);
        String type = ClientRewardStorage.REWARD_STORAGE.getRewards().get(entity.getUUID()).getEnabled().get(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "cat_ears"));
        stack.rotate(Axis.YP.rotationDegrees(90));
        stack.rotate(Axis.XP.rotationDegrees(180));
        stack.rotate(Axis.YN.rotationDegrees(netHeadYaw));
        stack.rotate(Axis.ZN.rotationDegrees(headPitch));
        Calendar calendar = Calendar.getInstance();
        if ((calendar.get(Calendar.MONTH) == Calendar.OCTOBER && type.equalsIgnoreCase("normal")) || type.equalsIgnoreCase("spooky")) {
            spookyScarySkeletons(stack, collector, lightCoords);
        } else if ((calendar.get(Calendar.MONTH) == Calendar.DECEMBER && type.equalsIgnoreCase("normal")) || type.equalsIgnoreCase("snowy")) {
            itsSnowyHere(stack, collector, lightCoords);
        } else {
            BlockStateModelPart ears = Minecraft.getInstance().getModelManager().getStandaloneModel(EARS_MODEL);
            if (ears != null) submitParts(stack, collector, List.of(ears), lightCoords);
        }
        stack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void spookyScarySkeletons(PoseStack stack, SubmitNodeCollector collector, int lightCoords) {
        List<BlockStateModelPart> pumpkin = blockParts(Minecraft.getInstance().level.getGameTime() % 200 < 100 ? Blocks.CARVED_PUMPKIN.defaultBlockState() : Blocks.JACK_O_LANTERN.defaultBlockState());
        stack.rotate(Axis.YN.rotationDegrees(90f));
        stack.translate(0.08f, 0.485f, -0.1f);
        stack.scale(2 / 16f, 2 / 16f, 2 / 16f);

        submitParts(stack, collector, pumpkin, lightCoords);
        stack.translate(-0.08f * 28f, 0f, 0f);
        submitParts(stack, collector, pumpkin, lightCoords);
    }

    @OnlyIn(Dist.CLIENT)
    public void itsSnowyHere(PoseStack stack, SubmitNodeCollector collector, int lightCoords) {
        List<BlockStateModelPart> pumpkin = blockParts(Blocks.CAKE.defaultBlockState());
        stack.rotate(Axis.YN.rotationDegrees(90f));
        stack.translate(0.08f, 0.485f, -0.1f);
        stack.scale(2 / 16f, 2 / 16f, 2 / 16f);

        submitParts(stack, collector, pumpkin, lightCoords);
        stack.translate(-0.08f * 28f, 0f, 0f);
        submitParts(stack, collector, pumpkin, lightCoords);
    }

    private static List<BlockStateModelPart> blockParts(BlockState state) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state).collectParts(RandomSource.create(42L), parts);
        return parts;
    }

    private static void submitParts(PoseStack stack, SubmitNodeCollector collector, List<BlockStateModelPart> parts, int lightCoords) {
        collector.submitBlockModel(stack, Sheets.cutoutBlockItemSheet(), parts, BlockModelRenderState.EMPTY_TINTS, lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

}
