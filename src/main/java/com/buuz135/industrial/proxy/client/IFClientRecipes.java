package com.buuz135.industrial.proxy.client;

import java.util.Collection;
import com.buuz135.industrial.utils.Reference;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.List;

/**
 * 26.3: the recipes the server synced to this client ({@code IFRecipeSync}). Recipe viewers read them from here; the
 * client level's recipe access holds none. Received before JEI starts (JEI listens at the lowest priority).
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class IFClientRecipes {

    private static RecipeMap recipes = RecipeMap.EMPTY;

    private IFClientRecipes() {
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = RecipeMap.EMPTY;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe<?>> List<T> getRecipes(RecipeType<T> type) {
        Collection<RecipeHolder<T>> holders = recipes.byType((RecipeType) type);
        return holders.stream().map(RecipeHolder::value).toList();
    }
}
