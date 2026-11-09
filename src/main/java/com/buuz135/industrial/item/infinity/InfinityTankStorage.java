package com.buuz135.industrial.item.infinity;

import com.buuz135.industrial.gui.component.ItemStackTankScreenAddon;
import com.buuz135.industrial.utils.IFAttachments;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluid tanks stored in the {@link IFAttachments#INFINITY_TANKS} component of an item, one compound per tank keyed by
 * its name, holding the fluid under "Fluid" (the 1.21 FluidTank NBT layout, so existing items keep their contents).
 * Reads and writes go through the capability's {@link ItemAccess}.
 */
public class InfinityTankStorage extends ItemAccessResourceHandler<FluidResource> implements IScreenAddonProvider {

    private final List<TankDefinition> tanks;

    public InfinityTankStorage(ItemAccess access, TankDefinition... tanks) {
        super(access, tanks.length);
        this.tanks = List.of(tanks);
    }

    private static RegistryOps<Tag> ops() {
        return IFAttachments.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    private FluidStack getStored(ItemResource accessResource, int index) {
        CompoundTag component = accessResource.get(IFAttachments.INFINITY_TANKS.get());
        if (component == null) return FluidStack.EMPTY;
        return component.getCompoundOrEmpty(tanks.get(index).name()).read("Fluid", FluidStack.OPTIONAL_CODEC, ops()).orElse(FluidStack.EMPTY);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        return FluidResource.of(getStored(accessResource, index));
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        return getStored(accessResource, index).getAmount();
    }

    @Override
    protected @Nullable ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        CompoundTag component = accessResource.getOrDefault(IFAttachments.INFINITY_TANKS.get(), new CompoundTag()).copy();
        CompoundTag tank = new CompoundTag();
        if (newAmount > 0 && !newResource.isEmpty()) {
            tank.store("Fluid", FluidStack.OPTIONAL_CODEC, ops(), newResource.toStack(newAmount));
        }
        component.put(tanks.get(index).name(), tank);
        return accessResource.with(IFAttachments.INFINITY_TANKS.get(), component);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return resource.isEmpty() || tanks.get(index).filter().test(resource.toStack(1));
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return tanks.get(index).capacity();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (!tanks.get(index).insert()) return 0;
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (!tanks.get(index).extract()) return 0;
        return super.extract(index, resource, amount, transaction);
    }

    /**
     * Extracts from a tank for the item's own use, ignoring the tank's external {@code extract} flag (the backpack's meat
     * tank refuses pipes and players but feeds its wearer).
     */
    public int extractInternal(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return super.extract(index, resource, amount, transaction);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull List<IFactory<?>> getScreenAddons() {
        List<IFactory<?>> list = new ArrayList<>();
        for (int i = 0; i < this.tanks.size(); i++) {
            var tankDefinition = this.tanks.get(i);
            int finalI = i;
            list.add(() -> new ItemStackTankScreenAddon(tankDefinition.x, tankDefinition.y, this, finalI, tankDefinition.type, tankDefinition.definedFluidStack));
        }
        return list;
    }

    public record TankDefinition(String name, int capacity, int x, int y, Predicate<FluidStack> filter, boolean extract,
                                 boolean insert, FluidTankComponent.Type type, FluidStack definedFluidStack) {
    }
}
