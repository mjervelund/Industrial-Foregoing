package com.buuz135.industrial.recipe;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.utils.Reference;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * 26.3: the client has no recipe manager, so recipe viewers only see the recipe types the server sends. Sends Industrial
 * Foregoing's machine recipes to every client on login and /reload ({@code IFClientRecipes} keeps them on the client).
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public final class IFRecipeSync {

    private IFRecipeSync() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(
                ModuleCore.DISSOLUTION_TYPE.get(),
                ModuleCore.FLUID_EXTRACTOR_TYPE.get(),
                ModuleCore.LASER_DRILL_TYPE.get(),
                ModuleCore.LASER_DRILL_FLUID_TYPE.get(),
                ModuleCore.STONEWORK_GENERATE_TYPE.get(),
                ModuleCore.CRUSHER_TYPE.get()
        );
    }
}
