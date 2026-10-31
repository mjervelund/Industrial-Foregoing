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

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// The 1.21 IItemHandler/IFluidHandler simulate-style calls on top of the 26.3 transactional ResourceHandlers
public class TransferUtils {

    public static ItemStack extractItem(ResourceHandler<ItemResource> handler, int slot, int amount, boolean simulate) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        try (var transaction = Transaction.open(null)) {
            int extracted = handler.extract(slot, resource, amount, transaction);
            if (!simulate) transaction.commit();
            return resource.toStack(extracted);
        }
    }

    public static int fill(ResourceHandler<FluidResource> handler, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        try (var transaction = Transaction.open(null)) {
            int filled = handler.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            if (!simulate) transaction.commit();
            return filled;
        }
    }

    public static FluidStack drain(ResourceHandler<FluidResource> handler, int amount, boolean simulate) {
        if (amount <= 0) return FluidStack.EMPTY;
        try (var transaction = Transaction.open(null)) {
            var drained = ResourceHandlerUtil.extractFirst(handler, resource -> true, amount, transaction);
            if (drained == null) return FluidStack.EMPTY;
            if (!simulate) transaction.commit();
            return drained.resource().toStack(drained.amount());
        }
    }

    public static FluidStack drain(ResourceHandler<FluidResource> handler, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        try (var transaction = Transaction.open(null)) {
            int drained = handler.extract(FluidResource.of(stack), stack.getAmount(), transaction);
            if (!simulate) transaction.commit();
            return stack.copyWithAmount(drained);
        }
    }
}
