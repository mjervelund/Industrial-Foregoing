package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.fluid.OreTitaniumFluidType;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.fluid.ClientFluidTypeExtensions;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * 26.3: {@link ClientFluidTypeExtensions} implements NeoForge's FluidTintSource, which extends the client-only
 * {@code net.minecraft.client.color.block.BlockTintSource}, so constructing one on a dedicated server throws
 * NoClassDefFoundError during mod construction. The fluid render properties are only read by the client
 * registrars, so they are built on the client only and are null on a dedicated server.
 */
public final class IFFluidRenderProperties {
    private IFFluidRenderProperties() {
    }

    public static IClientFluidTypeExtensions of(String still, String flow) {
        return FMLEnvironment.getDist().isClient() ? Client.plain(still, flow) : null;
    }

    public static IClientFluidTypeExtensions ore(String still, String flow) {
        return FMLEnvironment.getDist().isClient() ? Client.ore(still, flow) : null;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

    // Only loaded when the client branch runs.
    private static final class Client {
        static IClientFluidTypeExtensions plain(String still, String flow) {
            return new ClientFluidTypeExtensions(id(still), id(flow));
        }

        static IClientFluidTypeExtensions ore(String still, String flow) {
            return new OreTitaniumFluidType.Client(id(still), id(flow));
        }
    }
}
