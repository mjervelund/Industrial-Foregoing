package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.fluid.OreFluidInstance;
import com.hrznstudio.titanium.fluid.TitaniumFluidRenderProperties;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 26.3: fluid client extensions and fluid models are registered through mod bus events
 * (1.21 used FluidType#initializeClient). Mirrors Titanium's TitaniumFluidModelRegistrar for IF's ore fluids.
 */
public final class OreFluidClientRegistrar {
    private OreFluidClientRegistrar() {
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (OreFluidInstance instance : OreFluidInstance.getInstances()) {
            event.registerFluidType(instance.getRenderProperties(), instance.getFluidType().get());
        }
    }

    public static void registerModels(RegisterFluidModelsEvent event) {
        for (OreFluidInstance instance : OreFluidInstance.getInstances()) {
            if (!(instance.getRenderProperties() instanceof TitaniumFluidRenderProperties properties)) continue;
            event.register(
                new FluidModel.Unbaked(
                    new Material(properties.getStillTexture()),
                    new Material(properties.getFlowingTexture()),
                    properties.getOverlayTexture() == null ? null : new Material(properties.getOverlayTexture()),
                    properties.getTintSource()
                ),
                instance.getSourceFluid(),
                instance.getFlowingFluid()
            );
        }
    }
}
