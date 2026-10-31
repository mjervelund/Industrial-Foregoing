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

package com.buuz135.industrial.recipe;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.TankUtils;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.minecraft.core.Holder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DissolutionChamberRecipe implements IndustrialRecipe {

    public static final MapCodec<DissolutionChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Ingredient.CODEC.listOf(0, 8).fieldOf("input").forGetter(o -> o.input),
            SizedFluidIngredient.CODEC.fieldOf("inputFluid").forGetter(o -> o.inputFluid),
            Codec.INT.fieldOf("processingTime").forGetter(o -> o.processingTime),
            ItemStack.CODEC.optionalFieldOf("output").forGetter(o -> o.output),
            FluidStack.CODEC.optionalFieldOf("outputFluid").forGetter(o -> o.outputFluid)
    ).apply(in, DissolutionChamberRecipe::new));

    public List<Ingredient> input;
    public SizedFluidIngredient inputFluid;
    public int processingTime;
    public Optional<ItemStack> output;
    public Optional<FluidStack> outputFluid;

    public DissolutionChamberRecipe(List<Ingredient> input, SizedFluidIngredient inputFluid, int processingTime, Optional<ItemStack> output, Optional<FluidStack> outputFluid) {
        this.input = input;
        this.inputFluid = inputFluid;
        this.processingTime = processingTime;
        this.output = output;
        this.outputFluid = outputFluid;
    }

    public DissolutionChamberRecipe(List<Ingredient> input, FluidStack inputFluid, int processingTime, Optional<ItemStack> output, Optional<FluidStack> outputFluid) {
        this.input = input;
        this.inputFluid = SizedFluidIngredient.of(inputFluid.getFluid(), inputFluid.getAmount());
        this.processingTime = processingTime;
        this.output = output;
        this.outputFluid = outputFluid;
    }

    public DissolutionChamberRecipe() {

    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, DissolutionChamberRecipe recipe) {
        var rl = generateRL(name);
        List<ICondition> conditions = new ArrayList<>();
        if (recipe.output.isPresent())
            conditions.add(NeoForgeConditions.itemRegistered(BuiltInRegistries.ITEM.getKey(recipe.output.get().getItem())));
        IndustrialRecipe.save(recipeOutput, rl, recipe, conditions.toArray(new ICondition[conditions.size()]));
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "dissolution_chamber/" + key);
    }

    public boolean matches(InventoryComponent<?> handler, FluidTankComponent<?> tank) {
        if (input == null || tank == null || inputFluid == null) return false;
        List<ItemStack> handlerItems = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) handlerItems.add(handler.getStackInSlot(i).copy());
        }
        for (Ingredient ingredient : input) {
            boolean found = false;
            for (Holder<Item> item : ingredient.items().toList()) {
                int i = 0;
                for (; i < handlerItems.size(); i++) {
                    if (handlerItems.get(i).is(item)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    handlerItems.remove(i);
                    break;
                }
            }
            if (!found) return false;
        }
        // check the tank fluid is in ingredient
        boolean flag = inputFluid.test(tank.getFluid());

        return handlerItems.isEmpty() && flag && TankUtils.drainForced(tank, tank.getFluid().copyWithAmount(inputFluid.amount()), true).getAmount() == inputFluid.amount();
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output.orElse(ItemStack.EMPTY).copy();
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(ModuleCore.DISSOLUTION_CHAMBER.getBlock());
    }

    @Override
    public RecipeSerializer<? extends IndustrialRecipe> getSerializer() {
        return IndustrialRecipe.cast(ModuleCore.DISSOLUTION_SERIALIZER.get());
    }

    @Override
    public RecipeType<? extends IndustrialRecipe> getType() {
        return IndustrialRecipe.cast(ModuleCore.DISSOLUTION_TYPE.get());
    }
}
