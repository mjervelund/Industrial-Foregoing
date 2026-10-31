package com.buuz135.industrial.block;

import net.minecraft.core.HolderLookup;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class IndustrialBlockItem extends BlockItem{
    private TitaniumTab group;

    public IndustrialBlockItem(Block blockIn, TitaniumTab group) {
        super(blockIn, com.hrznstudio.titanium.module.DeferredRegistryHelper.applyItemRegistrationId(new Item.Properties().useBlockDescriptionPrefix()));
        this.group = group;
    }


    @Nullable
    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {
        return Component.translatable("itemGroup.industrialforegoing_" + this.group.getIdentifier().getPath()).getString();
    }
}
