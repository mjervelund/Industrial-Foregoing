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

package com.buuz135.industrial.recipe.provider;

import com.buuz135.industrial.module.ModuleAgricultureHusbandry;
import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.util.TagUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class IndustrialTagsProvider {

    private static ResourceKey<Block> key(Block block) {
        return block.builtInRegistryHolder().key();
    }

    private static ResourceKey<Item> key(ItemLike item) {
        return item.asItem().builtInRegistryHolder().key();
    }


    public static class Blocks extends BlockTagsProvider {

        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
            super(output, lookupProvider, modId);
        }

        @Override
        protected void addTags(HolderLookup.Provider p_256380_) {
            tag(IndustrialTags.Blocks.MACHINE_FRAME_PITY).add(key(ModuleCore.PITY.get()));
            tag(IndustrialTags.Blocks.MACHINE_FRAME_SIMPLE).add(key(ModuleCore.SIMPLE.get()));
            tag(IndustrialTags.Blocks.MACHINE_FRAME_ADVANCED).add(key(ModuleCore.ADVANCED.get()));
            tag(IndustrialTags.Blocks.MACHINE_FRAME_SUPREME).add(key(ModuleCore.SUPREME.get()));
            tag(BlockTags.DIRT).add(key(ModuleAgricultureHusbandry.HYDROPONIC_BED.getBlock()));
            tag(BlockTags.LUSH_GROUND_REPLACEABLE).remove(key(ModuleAgricultureHusbandry.HYDROPONIC_BED.getBlock()));
            tag(BlockTags.AZALEA_ROOT_REPLACEABLE).remove(key(ModuleAgricultureHusbandry.HYDROPONIC_BED.getBlock()));
            tag(BlockTags.SCULK_REPLACEABLE).remove(key(ModuleAgricultureHusbandry.HYDROPONIC_BED.getBlock()));
            tag(BlockTags.MOSS_REPLACEABLE).remove(key(ModuleAgricultureHusbandry.HYDROPONIC_BED.getBlock()));

            TagAppender<Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
            tag(TagUtil.getBlockTag(Identifier.parse("c:storage_blocks"))).add(key(ModuleCore.PINK_SLIME_BLOCK.get()));
            tag(TagUtil.getBlockTag(Identifier.parse("c:storage_blocks/pink_slime"))).add(key(ModuleCore.PINK_SLIME_BLOCK.get()));
            BuiltInRegistries.BLOCK.stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Reference.MOD_ID)).forEach(block -> tTagAppender.add(BuiltInRegistries.BLOCK.getResourceKey(block).get()));
        }
    }

    public static class Items extends BlockTagCopyingItemTagProvider {

        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagsProvider.TagLookup<Block>> lookupCompletableFuture, String modId) {
            super(output, completableFuture, lookupCompletableFuture, modId);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            this.copy(IndustrialTags.Blocks.MACHINE_FRAME_PITY, IndustrialTags.Items.MACHINE_FRAME_PITY);
            this.copy(IndustrialTags.Blocks.MACHINE_FRAME_SIMPLE, IndustrialTags.Items.MACHINE_FRAME_SIMPLE);
            this.copy(IndustrialTags.Blocks.MACHINE_FRAME_ADVANCED, IndustrialTags.Items.MACHINE_FRAME_ADVANCED);
            this.copy(IndustrialTags.Blocks.MACHINE_FRAME_SUPREME, IndustrialTags.Items.MACHINE_FRAME_SUPREME);

            tag(IndustrialTags.Items.PLASTIC).add(key(ModuleCore.PLASTIC.get()));
            tag(IndustrialTags.Items.SLUDGE_OUTPUT).add(key(net.minecraft.world.item.Items.DIRT)).add(key(net.minecraft.world.item.Items.CLAY)).add(key(net.minecraft.world.item.Items.GRAVEL)).add(key(net.minecraft.world.item.Items.SAND)).add(key(net.minecraft.world.item.Items.RED_SAND)).add(key(net.minecraft.world.item.Items.SOUL_SAND));
            tag(Tags.Items.SLIME_BALLS).add(key(ModuleCore.PINK_SLIME_ITEM.get()));
            tag(IndustrialTags.Items.GEAR_DIAMOND).add(key(ModuleCore.DIAMOND_GEAR.get()));
            tag(IndustrialTags.Items.GEAR_GOLD).add(key(ModuleCore.GOLD_GEAR.get()));
            tag(IndustrialTags.Items.GEAR_IRON).add(key(ModuleCore.IRON_GEAR.get()));
            tag(IndustrialTags.Items.GEARS).add(key(ModuleCore.DIAMOND_GEAR.get())).add(key(ModuleCore.GOLD_GEAR.get())).add(key(ModuleCore.IRON_GEAR.get()));

            tag(TagUtil.getItemTag(Identifier.parse("c:ingots"))).add(key(ModuleCore.PINK_SLIME_INGOT.get()));
            tag(TagUtil.getItemTag(Identifier.parse("c:ingots/pink_slime"))).add(key(ModuleCore.PINK_SLIME_INGOT.get()));
        }
    }

}
