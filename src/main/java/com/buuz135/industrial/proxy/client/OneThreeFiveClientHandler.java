package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.item.infinity.OneThreeFiveHandler;
import com.buuz135.industrial.proxy.client.particle.ParticleVex;
import com.buuz135.industrial.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client half of {@link OneThreeFiveHandler}: on 26.3 {@code @OnlyIn} no longer strips methods, so the client tick
 * handler in the common subscriber made the dedicated server load client particle classes.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class OneThreeFiveClientHandler {
    private OneThreeFiveClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.level() != null && !Minecraft.getInstance().isPaused() && Minecraft.getInstance().player.level().getGameTime() % 5 == 0) {
            BlockPos pos = new BlockPos(Minecraft.getInstance().player.blockPosition().getX(), Minecraft.getInstance().player.blockPosition().getY(), Minecraft.getInstance().player.blockPosition().getZ());
            Minecraft.getInstance().player.level().getEntitiesOfClass(LivingEntity.class, new AABB(Vec3.atCenterOf(pos.offset(32, 32, 32)), Vec3.atCenterOf(pos.offset(-32, -32, -32))),
                            input -> input.getUUID().toString().contains(OneThreeFiveHandler.SPECIAL)).
                    forEach(living -> Minecraft.getInstance().particleEngine.add(new ParticleVex(living)));
            Minecraft.getInstance().player.level().getEntitiesOfClass(Player.class, new AABB(Vec3.atCenterOf(pos.offset(32, 32, 32)), Vec3.atCenterOf(pos.offset(-32, -32, -32))),
                            input -> OneThreeFiveHandler.SPECIAL_ENTITIES.containsKey(input.getUUID())).
                    forEach(living -> Minecraft.getInstance().particleEngine.add(new ParticleVex(living)));
        }
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : OneThreeFiveHandler.SPECIAL_ENTITIES.keySet()) {
            if (System.currentTimeMillis() >= OneThreeFiveHandler.SPECIAL_ENTITIES.get(uuid) + 1000) {
                toRemove.add(uuid);
            }
        }
        toRemove.forEach(uuid -> OneThreeFiveHandler.SPECIAL_ENTITIES.remove(uuid));
    }
}
