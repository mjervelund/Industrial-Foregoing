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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StoneWorkGenerateRecipe implements IndustrialRecipe {

    public static final MapCodec<StoneWorkGenerateRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ItemStack.CODEC.fieldOf("output").forGetter(o -> o.output),
            Codec.INT.fieldOf("waterNeed").forGetter(o -> o.waterNeed),
            Codec.INT.fieldOf("lavaNeed").forGetter(o -> o.lavaNeed),
            Codec.INT.fieldOf("waterConsume").forGetter(o -> o.waterConsume),
            Codec.INT.fieldOf("lavaConsume").forGetter(o -> o.lavaConsume)
    ).apply(in, StoneWorkGenerateRecipe::new));
    public ItemStack output;
    public int waterNeed;
    public int lavaNeed;
    public int waterConsume;
    public int lavaConsume;
    public StoneWorkGenerateRecipe(ItemStack output, int waterNeed, int lavaNeed, int waterConsume, int lavaConsume) {
        this.output = output;
        this.waterNeed = waterNeed;
        this.lavaNeed = lavaNeed;
        this.waterConsume = waterConsume;
        this.lavaConsume = lavaConsume;
    }
    public StoneWorkGenerateRecipe() {
    }

    public static void init(RecipeOutput output) {
        createRecipe(output, "cobblestone", new StoneWorkGenerateRecipe(new ItemStack(Blocks.COBBLESTONE), 1000, 1000, 0, 0));
        createRecipe(output, "netherrack", new StoneWorkGenerateRecipe(new ItemStack(Blocks.NETHERRACK), 250, 400, 250, 200));
        createRecipe(output, "obsidian", new StoneWorkGenerateRecipe(new ItemStack(Blocks.OBSIDIAN), 1000, 1000, 0, 1000));
        createRecipe(output, "granite", new StoneWorkGenerateRecipe(new ItemStack(Blocks.GRANITE), 200, 200, 200, 200));
        createRecipe(output, "diorite", new StoneWorkGenerateRecipe(new ItemStack(Blocks.DIORITE), 200, 250, 200, 250));
        createRecipe(output, "andesite", new StoneWorkGenerateRecipe(new ItemStack(Blocks.ANDESITE), 300, 300, 300, 300));
    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, StoneWorkGenerateRecipe recipe) {
        var rl = generateRL(name);
        IndustrialRecipe.save(recipeOutput, rl, recipe);
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "stonework_generate/" + key);
    }

    @Override
    public RecipeSerializer<? extends IndustrialRecipe> getSerializer() {
        return IndustrialRecipe.cast(ModuleCore.STONEWORK_GENERATE_SERIALIZER.get());
    }

    @Override
    public RecipeType<? extends IndustrialRecipe> getType() {
        return IndustrialRecipe.cast(ModuleCore.STONEWORK_GENERATE_TYPE.get());
    }

    public boolean canIncrease(FluidTankComponent<?> fluidTank, FluidTankComponent<?> fluidTank2) {
        return fluidTank.getFluidAmount() >= waterNeed && fluidTank2.getFluidAmount() >= lavaNeed;
    }

    public void consume(FluidTankComponent<?> fluidTank, FluidTankComponent<?> fluidTank2) {
        TankUtils.drainForced(fluidTank, waterConsume, false);
        TankUtils.drainForced(fluidTank2, lavaConsume, false);
    }

}
