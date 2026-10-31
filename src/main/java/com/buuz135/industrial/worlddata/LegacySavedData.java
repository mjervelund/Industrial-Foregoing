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

package com.buuz135.industrial.worlddata;

import com.buuz135.industrial.IndustrialForegoing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saved data ids are lowercase {@code Identifier}s now, so IF's data moved from {@code data/IFBackpack.dat} to
 * {@code data/industrialforegoing/backpack.dat}. When the new file does not exist yet, the managers read the old one
 * (same {@code data} layout) and save it under the new name.
 */
public final class LegacySavedData {

    private LegacySavedData() {
    }

    @Nullable
    public static CompoundTag read(@Nullable ServerLevel level, String legacyName) {
        if (level == null) return null;
        String file = legacyName + ".dat";
        Path[] candidates = {
                level.getServer().getWorldPath(LevelResource.DATA).resolve(file),
                level.getServer().getWorldPath(LevelResource.ROOT).resolve("dimensions/minecraft/overworld/data").resolve(file)
        };
        for (Path path : candidates) {
            if (!Files.isRegularFile(path)) continue;
            try {
                CompoundTag data = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()).getCompoundOrEmpty("data");
                IndustrialForegoing.LOGGER.info("Migrating legacy saved data {}", path);
                return data;
            } catch (IOException e) {
                IndustrialForegoing.LOGGER.error("Could not read legacy saved data {}", path, e);
            }
        }
        return null;
    }
}
