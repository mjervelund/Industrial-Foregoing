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

import net.minecraft.core.registries.BuiltInRegistries;
import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.utils.Reference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class FluidExtractorRecipe implements IndustrialRecipe {

    public static final MapCodec<FluidExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Ingredient.CODEC.fieldOf("input").forGetter(o -> o.input),
            BlockState.CODEC.fieldOf("result").forGetter(o -> o.result),
            Codec.FLOAT.fieldOf("breakChance").forGetter(o -> o.breakChance),
            FluidStackTemplate.CODEC.fieldOf("output").forGetter(o -> o.output),
            Codec.BOOL.fieldOf("defaultRecipe").forGetter(o -> o.defaultRecipe)
    ).apply(in, FluidExtractorRecipe::fromCodec));

    public Ingredient input;
    public BlockState result;
    public float breakChance;
    // 26.3: recipes are parsed with the datapack registries, before item/fluid components are bound, so outputs are
    // stored as ItemStackTemplate/FluidStackTemplate (same JSON shape) and stacks are created on use.
    public FluidStackTemplate output;
    public boolean defaultRecipe;

    public FluidExtractorRecipe(Ingredient input, BlockState result, float breakChance, FluidStack output, boolean defaultRecipe) {
        this.input = input;
        this.result = result;
        this.breakChance = breakChance;
        this.output = FluidStackTemplate.fromNonEmptyStack(output);
        this.defaultRecipe = defaultRecipe;
    }
    public FluidExtractorRecipe() {
    }

    private static FluidExtractorRecipe fromCodec(Ingredient input, BlockState result, float breakChance, FluidStackTemplate output, boolean defaultRecipe) {
        FluidExtractorRecipe recipe = new FluidExtractorRecipe();
        recipe.input = input;
        recipe.result = result;
        recipe.breakChance = breakChance;
        recipe.output = output;
        recipe.defaultRecipe = defaultRecipe;
        return recipe;
    }

    /** A new stack of the fluid output. */
    public FluidStack getOutput() {
        return output.create();
    }

    public static void init(RecipeOutput output) {
        createRecipe(output, "acacia", new FluidExtractorRecipe(Ingredient.of(Blocks.ACACIA_LOG), Blocks.STRIPPED_ACACIA_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 4), false));
        createRecipe(output, "mangrove", new FluidExtractorRecipe(Ingredient.of(Blocks.MANGROVE_LOG), Blocks.STRIPPED_MANGROVE_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 4), false));
        createRecipe(output, "dark_oak", new FluidExtractorRecipe(Ingredient.of(Blocks.DARK_OAK_LOG), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 3), false));
        createRecipe(output, "cherry", new FluidExtractorRecipe(Ingredient.of(Blocks.CHERRY_LOG), Blocks.STRIPPED_CHERRY_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 3), false));
        createRecipe(output, "oak", new FluidExtractorRecipe(Ingredient.of(Blocks.OAK_LOG), Blocks.STRIPPED_OAK_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 2), false));
        createRecipe(output, "spruce", new FluidExtractorRecipe(Ingredient.of(Blocks.SPRUCE_LOG), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 2), false));
        createRecipe(output, "birch", new FluidExtractorRecipe(Ingredient.of(Blocks.BIRCH_LOG), Blocks.STRIPPED_BIRCH_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 2), false));
        createRecipe(output, "jungle", new FluidExtractorRecipe(Ingredient.of(Blocks.JUNGLE_LOG), Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 2), false));
        createRecipe(output, "default", new FluidExtractorRecipe(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.LOGS)), Blocks.AIR.defaultBlockState(), 0.010f, new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 1), true));
    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, FluidExtractorRecipe recipe) {
        var rl = generateRL(name);
        IndustrialRecipe.save(recipeOutput, rl, recipe);
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "fluid_extractor/" + key);
    }

    public boolean outputsLatex() {
        return output.fluid().value().isSame(ModuleCore.LATEX.getSourceFluid().get());
    }

    public boolean matches(Level world, BlockPos pos) {
        return input.test(new ItemStack(world.getBlockState(pos).getBlock()));
    }

    @Override
    public RecipeSerializer<? extends IndustrialRecipe> getSerializer() {
        return IndustrialRecipe.cast(ModuleCore.FLUID_EXTRACTOR_SERIALIZER.get());
    }

    @Override
    public RecipeType<? extends IndustrialRecipe> getType() {
        return IndustrialRecipe.cast(ModuleCore.FLUID_EXTRACTOR_TYPE.get());
    }

}
