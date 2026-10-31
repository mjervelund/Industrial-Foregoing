package com.buuz135.industrial.proxy.client.sound;

import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.module.ModuleTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Client sounds of the infinity nuke, moved out of {@link InfinityNukeEntity}: on 26.3 {@code @OnlyIn} no longer
 * strips methods, so the entity's client methods made the dedicated server load client sound classes.
 * Only call from client-side code.
 */
public final class NukeClientSounds {
    private NukeClientSounds() {
    }

    private static final class State {
        TickeableSound charging;
        TickeableSound exploding;
    }

    public static void tick(InfinityNukeEntity nuke) {
        State state = nuke.getClientSounds() instanceof State s ? s : null;
        if (state == null) {
            state = new State();
            nuke.setClientSounds(state);
        }
        var soundManager = Minecraft.getInstance().getSoundManager();
        if (state.charging == null && nuke.isExplodingSynced()) {
            soundManager.play(state.charging = new TickeableSound(nuke.level(), nuke.blockPosition(), ModuleTool.NUKE_CHARGING.get(), nuke.getRadius(), 10, nuke.level().getRandom()));
        }
        if (state.charging != null) {
            state.charging.setDistance(nuke.getRadius());
            state.charging.increase();
            if (!soundManager.isActive(state.charging) && state.exploding == null) {
                state.exploding = new TickeableSound(nuke.level(), nuke.blockPosition(), ModuleTool.NUKE_EXPLOSION.get(), nuke.getRadius(), 10, nuke.level().getRandom());
                state.exploding.setPitch(1);
                soundManager.play(state.exploding);
            }
        }
    }

    public static void arm(InfinityNukeEntity nuke) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(ModuleTool.NUKE_ARMING.get(), SoundSource.BLOCKS, 1, 1, Minecraft.getInstance().level.getRandom(), nuke.blockPosition()));
    }
}
