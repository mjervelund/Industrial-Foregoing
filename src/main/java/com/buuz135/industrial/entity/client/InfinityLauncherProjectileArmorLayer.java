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
package com.buuz135.industrial.entity.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

public class InfinityLauncherProjectileArmorLayer<M extends PlayerModel> extends StuckInBodyLayer<M, Unit> {

    public static HashMap<String, Integer> PROJECTILE_AMOUNT = new HashMap<>();

    public InfinityLauncherProjectileArmorLayer(LivingEntityRenderer<?, AvatarRenderState, M> renderer, EntityModelSet models) {
        super(renderer, new InfinityLauncherProjectileModel(models.bakeLayer(InfinityLauncherProjectileRenderer.PROJECTILE_LAYER)), Unit.INSTANCE,
                InfinityLauncherProjectileRenderer.PROJECTILE, PlacementStyle.IN_CUBE);
    }

    @Override
    protected int numStuck(AvatarRenderState state) {
        // The render state carries no UUID; the counts are keyed by player UUID
        Entity entity = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(state.id);
        return entity == null ? 0 : PROJECTILE_AMOUNT.getOrDefault(entity.getUUID().toString(), 0);
    }
}
