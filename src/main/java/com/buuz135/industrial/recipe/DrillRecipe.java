package com.buuz135.industrial.recipe;

import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.recipe.data.EntityData;
import com.buuz135.industrial.utils.Reference;

import java.util.*;

public abstract class DrillRecipe<I> implements IndustrialRecipe {

    public static Identifier EMPTY = Identifier.fromNamespaceAndPath("minecraft", "empty");
    
    public I output;
    public List<LaserDrillRarity> rarity;
    public int pointer = 0;
    public Ingredient catalyst;
    public Optional<EntityData> entityData;

    public DrillRecipe(I output, Ingredient catalyst, Optional<EntityData> data, List<LaserDrillRarity> rarity) {
        this.output = output;
        this.rarity = rarity;
        this.catalyst = catalyst;
        this.entityData = data;
    }
    public DrillRecipe(I output, Ingredient catalyst, Optional<EntityData> data, LaserDrillRarity... rarity) {
        this(output, catalyst, data, Arrays.asList(rarity));
    }
    public DrillRecipe(I output, int color, Optional<EntityData> data, LaserDrillRarity... rarity) {
        this(output, Ingredient.of(ModuleCore.LASER_LENS[color].get()), data, rarity);
    }

    public DrillRecipe(I output, int color, LaserDrillRarity... rarity) {
        this(output, Ingredient.of(ModuleCore.LASER_LENS[color].get()), Optional.empty(), rarity);
    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, String modIdCondition, DrillRecipe<?> recipe) {
        var rl = generateRL(name);
        IndustrialRecipe.save(recipeOutput, rl, recipe, new ModLoadedCondition(modIdCondition));
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "laser_drill_fluid/" + key);
    }
}
