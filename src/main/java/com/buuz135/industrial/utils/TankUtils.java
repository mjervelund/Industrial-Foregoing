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

import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// The 1.21 fill/drain(FluidStack|int, FluidAction) tank calls on top of the 26.3 transactional tanks
public class TankUtils {

    public static int fillForced(FluidTankComponent<?> tank, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        try (var transaction = Transaction.open(null)) {
            int filled = tank.insertForced(FluidResource.of(stack), stack.getAmount(), transaction);
            if (!simulate) transaction.commit();
            return filled;
        }
    }

    public static FluidStack drainForced(FluidTankComponent<?> tank, int amount, boolean simulate) {
        return drainForced(tank, tank.getFluid().copyWithAmount(amount), simulate);
    }

    public static FluidStack drainForced(FluidTankComponent<?> tank, FluidStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getAmount() <= 0) return FluidStack.EMPTY;
        FluidResource resource = FluidResource.of(stack);
        try (var transaction = Transaction.open(null)) {
            int drained = tank.extractForced(resource, stack.getAmount(), transaction);
            if (!simulate) transaction.commit();
            return drained == 0 ? FluidStack.EMPTY : resource.toStack(drained);
        }
    }

    public static int fill(FluidTankComponent<?> tank, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        try (var transaction = Transaction.open(null)) {
            int filled = tank.insert(0, FluidResource.of(stack), stack.getAmount(), transaction);
            if (!simulate) transaction.commit();
            return filled;
        }
    }

    public static FluidStack drain(FluidTankComponent<?> tank, int amount, boolean simulate) {
        FluidStack stack = tank.getFluid();
        if (stack.isEmpty() || amount <= 0) return FluidStack.EMPTY;
        FluidResource resource = FluidResource.of(stack);
        try (var transaction = Transaction.open(null)) {
            int drained = tank.extract(0, resource, amount, transaction);
            if (!simulate) transaction.commit();
            return drained == 0 ? FluidStack.EMPTY : resource.toStack(drained);
        }
    }
}
