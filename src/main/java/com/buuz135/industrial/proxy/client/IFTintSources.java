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

package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.item.MobImprisonmentToolItem;
import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.ItemInfinity;
import com.buuz135.industrial.module.ModuleTool;
import com.buuz135.industrial.utils.FluidUtils;
import com.buuz135.industrial.utils.IFAttachments;
import com.buuz135.industrial.utils.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

/**
 * Item tints referenced from the client item JSON (26.3 replaced ItemColors with data-driven tint sources).
 */
public class IFTintSources {

    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_tier"), InfinityTierTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "mob_imprisonment"), MobImprisonmentTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "fluid_contents"), FluidContentsTint.MAP_CODEC);
    }

    /**
     * Colour of the infinity tool's current tier.
     */
    public record InfinityTierTint() implements ItemTintSource {
        public static final InfinityTierTint INSTANCE = new InfinityTierTint();
        public static final MapCodec<InfinityTierTint> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return ARGB.opaque(InfinityTier.getTierBraquet(ItemInfinity.getPowerFromStack(stack)).getLeft().getTextureColor());
        }

        @Override
        public MapCodec<InfinityTierTint> type() {
            return MAP_CODEC;
        }
    }

    /**
     * Layer 1/2: colours of the captured mob, layer 3: the dot, red when the mob is blacklisted.
     * Spawn eggs no longer have colours in 26.3, so the mob colours are derived from the entity id.
     */
    public record MobImprisonmentTint(int layer) implements ItemTintSource {
        public static final MapCodec<MobImprisonmentTint> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.INT.fieldOf("layer").forGetter(MobImprisonmentTint::layer)
        ).apply(i, MobImprisonmentTint::new));

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (!stack.has(IFAttachments.MOB_IMPRISONMENT_TOOL)) return 0xFF636363;
            var tag = stack.get(IFAttachments.MOB_IMPRISONMENT_TOOL);
            Identifier id = Identifier.tryParse(tag.getStringOr("entity", ""));
            var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(id);
            if (type == null) return 0xFF636363;
            if (layer == 3) {
                return ((MobImprisonmentToolItem) ModuleTool.MOB_IMPRISONMENT_TOOL.get()).isBlacklisted(type) ? 0xFFDB201A : 0xFF636363;
            }
            int hash = id.hashCode() * (layer == 1 ? 31 : 17);
            return ARGB.opaque(hash & 0xFFFFFF);
        }

        @Override
        public MapCodec<MobImprisonmentTint> type() {
            return MAP_CODEC;
        }
    }

    /**
     * Colour of the contained fluid, texture colour times tint like {@link FluidUtils#getFluidColor}.
     */
    public record FluidContentsTint() implements ItemTintSource {
        public static final FluidContentsTint INSTANCE = new FluidContentsTint();
        public static final MapCodec<FluidContentsTint> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            var fluid = FluidUtil.getFirstStackContained(stack);
            if (fluid.isEmpty()) return 0xFFFFFFFF;
            return ARGB.opaque(FluidUtils.getFluidColor(fluid));
        }

        @Override
        public MapCodec<FluidContentsTint> type() {
            return MAP_CODEC;
        }
    }
}
