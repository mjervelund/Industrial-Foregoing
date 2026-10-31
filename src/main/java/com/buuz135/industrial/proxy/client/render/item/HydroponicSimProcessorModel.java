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

package com.buuz135.industrial.proxy.client.render.item;

import com.buuz135.industrial.item.HydroponicSimulationProcessorItem;
import com.buuz135.industrial.utils.IFAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Item model of the hydroponic simulation processor (was HydroponicSimProcessorISTER): the base model with the
 * simulated crop drawn small in its corner; holding shift in a GUI shows only the crop, full size.
 */
public class HydroponicSimProcessorModel implements ItemModel {

    private final ItemModel base;

    public HydroponicSimProcessorModel(ItemModel base) {
        this.base = base;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        boolean shift = Minecraft.getInstance().hasShiftDown();
        boolean gui = displayContext == ItemDisplayContext.GUI;
        output.appendModelIdentityElement(shift);
        ItemStack crop = ItemStack.EMPTY;
        if (stack.has(IFAttachments.HYDROPONIC_SIMULATION_PROCESSOR)) {
            crop = new HydroponicSimulationProcessorItem.Simulation(stack.get(IFAttachments.HYDROPONIC_SIMULATION_PROCESSOR)).getCrop();
        }
        if (!shift || !gui || crop.isEmpty()) base.update(output, stack, resolver, displayContext, level, owner, seed);
        if (!crop.isEmpty()) {
            ItemStackRenderState cropState = new ItemStackRenderState();
            resolver.updateForTopItem(cropState, crop, displayContext, level, owner, seed);
            output.appendModelIdentityElement(crop.getItem());
            output.newLayer().setupSpecialModel(CropRenderer.INSTANCE, new Crop(cropState, shift, gui));
        }
    }

    private record Crop(ItemStackRenderState state, boolean shift, boolean gui) {
    }

    private static class CropRenderer implements SpecialModelRenderer<Crop> {
        private static final CropRenderer INSTANCE = new CropRenderer();

        @Override
        public void submit(@Nullable Crop crop, PoseStack matrix, SubmitNodeCollector collector, int light, int overlayLight, boolean hasFoil, int outlineColor) {
            if (crop == null) return;
            matrix.pushPose();
            if (crop.gui()) matrix.translate(0, 0, 0.05);
            if (!crop.shift()) {
                if (crop.gui()) matrix.translate(0.25, -0.25, 0);
                else matrix.translate(0.1, -0.1, 0.001);
                matrix.scale(0.5f, 0.5f, 1);
            }
            crop.state().submit(matrix, collector, light, overlayLight, outlineColor);
            matrix.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable Crop extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(ItemModel.Unbaked base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemModels.CODEC.fieldOf("base").forGetter(Unbaked::base)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            base.resolveDependencies(resolver);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return new HydroponicSimProcessorModel(base.bake(context, transformation));
        }
    }
}
