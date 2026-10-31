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

package com.buuz135.industrial.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;

// 26.3 fuel is the COOKING_FUEL data component; its burn time may reference a context_int_provider
// (vanilla ones divide by a reduction factor that only differs for smokers/blast furnaces)
public class FuelUtils {

    private static final ContextKeySet FUEL_PARAMS = new ContextKeySet.Builder().optional(NeoForgeLootContextParams.QUERIED_STACK).build();

    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL);
    }

    // Furnace burn time in ticks, 0 if not a fuel (ItemStack#getBurnTime(RecipeType.SMELTING) on 1.21)
    public static int getBurnTime(ItemStack stack) {
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) return 0;
        if (fuel.burnTime() instanceof ResolvableInt.Constant constant) return constant.value();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        LootContext context = new LootContext.Builder(new LootParams.Builder(server.overworld())
                .withOptionalParameter(NeoForgeLootContextParams.QUERIED_STACK, stack)
                .create(FUEL_PARAMS)).create(Optional.empty());
        return fuel.burnTime().get(context, 0);
    }

    // ItemStack#getCraftingRemainingItem on 1.21
    public static ItemStack getCraftingRemainder(ItemStack stack) {
        ItemStackTemplate remainder = stack.getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }
}
