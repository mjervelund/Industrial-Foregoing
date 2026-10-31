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

package com.buuz135.industrial.item;

import com.buuz135.industrial.api.straw.StrawHandler;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.StrawUtils;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nonnull;
import java.util.Optional;

;

public class ItemStraw extends IFCustomItem {

    public ItemStraw(TitaniumTab group) {
        super("straw", group, new Properties().stacksTo(1));
    }

    @Override
    @Nonnull
    public ItemStack finishUsingItem(@Nonnull ItemStack heldStack, Level world, LivingEntity entity) {
        if (!world.isClientSide() && entity instanceof Player) {
            Player player = (Player) entity;
            HitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRayTraceResult = (BlockHitResult) result;
                BlockPos pos = blockRayTraceResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                FluidState fluidState = state.getFluidState();
                if (fluidState != Fluids.EMPTY.defaultFluidState() && block instanceof BucketPickup && fluidState.isSource()) {
                    StrawUtils.getStrawHandler(fluidState.getType()).ifPresent(handler -> {
                        ItemStack stack = ((BucketPickup) block).pickupBlock(player, world, pos, state);
                        FluidStack contained = FluidUtil.getFirstStackContained(stack);
                        if (!contained.isEmpty()) {
                            handler.onDrink(world, pos, contained.getFluid(), player, false);
                        }
                    });
                    return heldStack;
                }
                ResourceHandler<FluidResource> handler = world.getCapability(Capabilities.Fluid.BLOCK, pos, null);
                if (handler != null) {
                        int tanks = handler.size();
                        for (int i = 0; i < tanks; i++) {
                            FluidStack stack = FluidUtil.getStack(handler, i);
                            if (!stack.isEmpty()) {
                                Fluid fluidInstance = stack.getFluid();
                                Optional<StrawHandler> strawHandler = StrawUtils.getStrawHandler(fluidInstance);
                                if (fluidInstance != null && strawHandler.isPresent() && stack.getAmount() >= 1000) {
                                    try (var tx = Transaction.open(null)) {
                                        if (handler.extract(FluidResource.of(stack), 1000, tx) >= 1000) {
                                            strawHandler.ifPresent(straw -> straw.onDrink(world, pos, fluidInstance, player, true));
                                            tx.commit();
                                        }
                                    }
                                }
                            }
                        }

                }
            }
        }
        return super.finishUsingItem(heldStack, world, entity);
    }

    @Override
    @Nonnull
    public InteractionResult use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn) {
        HitResult result = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.SOURCE_ONLY);
        if (result != null && result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) result;
            BlockPos pos = blockRayTraceResult.getBlockPos();
            BlockState state = worldIn.getBlockState(pos);
            Block block = state.getBlock();
            FluidState fluid = state.getFluidState();//FluidRegistry.lookupFluidForBlock(block);
            if (fluid != null) {
                Optional<StrawHandler> handler = StrawUtils.getStrawHandler(fluid.getType());
                if (handler.isPresent()) {
                    playerIn.startUsingItem(handIn);
                    return InteractionResult.SUCCESS.heldItemTransformedTo(playerIn.getItemInHand(handIn)); //success accepted
                }
            }
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile != null) {
                ResourceHandler<FluidResource> handler = worldIn.getCapability(Capabilities.Fluid.BLOCK, pos, null);
                if (handler != null) {
                    int tanks = handler.size();
                    for (int i = 0; i < tanks; i++) {
                        FluidStack stack = FluidUtil.getStack(handler, i);
                        if (!stack.isEmpty()) {
                            Fluid fluidInstance = stack.getFluid();
                            Optional<StrawHandler> strawHandler = StrawUtils.getStrawHandler(fluidInstance);
                            if (fluidInstance != null && strawHandler.isPresent() && stack.getAmount() >= 1000) {
                                int out;
                                try (var tx = Transaction.open(null)) {
                                    out = handler.extract(FluidResource.of(stack), stack.getAmount(), tx);
                                }
                                if (out >= 1000) {
                                    playerIn.startUsingItem(handIn);
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_, LivingEntity entity) {
        return 30;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack p_77661_1_) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public void registerRecipe(RecipeOutput consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("PP ").pattern(" P ").pattern(" P ")
                .define('P', IndustrialTags.Items.PLASTIC)
                .save(consumer);
    }
}
