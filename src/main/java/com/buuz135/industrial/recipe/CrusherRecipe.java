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
import com.hrznstudio.titanium.util.TagUtil;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

import java.util.ArrayList;
import java.util.List;

public class CrusherRecipe implements IndustrialRecipe {

    public static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Ingredient.CODEC.fieldOf("input").forGetter(crusherRecipe -> crusherRecipe.input),
            Ingredient.CODEC.fieldOf("output").forGetter(crusherRecipe -> crusherRecipe.output)
    ).apply(in, CrusherRecipe::new));

    public CrusherRecipe(Ingredient input, Ingredient output) {
        this(input, output, null);
    }

    public CrusherRecipe(Ingredient input, Ingredient output, Identifier isTag) {
        this.input = input;
        this.output = output;
        this.isTag = isTag;
    }

    public CrusherRecipe() {
    }

    public Ingredient input;
    public Ingredient output;
    private Identifier isTag;

    public static void init(RecipeOutput output) {
        createRecipe(output, "cobblestone", new CrusherRecipe(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagUtil.getItemTag(Identifier.fromNamespaceAndPath("c", "cobblestones/normal")))), Ingredient.of(Items.GRAVEL)));
        createRecipe(output, "gravel", new CrusherRecipe(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagUtil.getItemTag(Identifier.fromNamespaceAndPath("c", "gravels")))), Ingredient.of(Items.SAND)));
        createRecipe(output, "sand", new CrusherRecipe(Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagUtil.getItemTag(Identifier.fromNamespaceAndPath("c", "sands")))), Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(TagUtil.getItemTag(Identifier.fromNamespaceAndPath("c", "silicon")))), Identifier.fromNamespaceAndPath("c", "silicon")));
    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, CrusherRecipe recipe) {
        var rl = generateRL(name);
        List<ICondition> conditions = new ArrayList<>();
        if (recipe.isTag != null) {
            conditions.add(new NotCondition(new TagEmptyCondition<>(TagUtil.getItemTag(recipe.isTag))));
        }
        IndustrialRecipe.save(recipeOutput, rl, recipe, conditions.toArray(new ICondition[conditions.size()]));
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "crusher/" + key);
    }

    @Override
    public RecipeSerializer<? extends IndustrialRecipe> getSerializer() {
        return IndustrialRecipe.cast(ModuleCore.CRUSHER_SERIALIZER.get());
    }

    @Override
    public RecipeType<? extends IndustrialRecipe> getType() {
        return IndustrialRecipe.cast(ModuleCore.CRUSHER_TYPE.get());
    }

}
