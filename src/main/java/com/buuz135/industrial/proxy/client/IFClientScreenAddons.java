package com.buuz135.industrial.proxy.client;

import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.client.screen.addon.AssetScreenAddon;
import com.hrznstudio.titanium.client.screen.addon.WidgetScreenAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;

/**
 * Screen addons that tiles create inside {@code initClient}. On 26.3 {@code @OnlyIn} strips nothing, and the server
 * links every tile class (Titanium scans its fields at block registration), so a tile method that uses a client widget
 * as a widget type (passes it on, calls it) makes the verifier load client GUI classes and the dedicated server
 * crashes. Building such addons here keeps the client types out of the tile classes.
 */
public final class IFClientScreenAddons {
    private IFClientScreenAddons() {
    }

    /**
     * A 40x12 number field at (80, 26) shown at (addonX, addonY): displays {@code value}, accepts only characters
     * matching {@code allowedChar} (EditBox#setFilter is gone on 26.3) and passes every edit to {@code responder}.
     */
    public static IScreenAddon numberField(int addonX, int addonY, IntSupplier value, IntPredicate allowedChar, Consumer<String> responder) {
        var edit = new EditBox(Minecraft.getInstance().font, 80, 26, 40, 12, Component.literal(value.getAsInt() + "")) {
            @Override
            public String getValue() {
                return value.getAsInt() + "";
            }

            @Override
            public void insertText(String input) {
                if (input.chars().allMatch(allowedChar)) super.insertText(input);
            }
        };
        edit.setValue(value.getAsInt() + "");
        edit.setResponder(responder);
        return new WidgetScreenAddon(addonX, addonY, edit);
    }

    public static IScreenAddon augmentBackground() {
        return new AssetScreenAddon(AssetTypes.AUGMENT_BACKGROUND, 175, 4, true);
    }
}
