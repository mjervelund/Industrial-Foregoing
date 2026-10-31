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

package com.buuz135.industrial.proxy.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ParticleVex extends Particle {

    // Line strips have no vanilla particle group in 26.3: the group is registered in ClientProxy
    public static final ParticleRenderType RENDER = new ParticleRenderType("industrialforegoing:vex", "IFV");

    private final Entity entity;
    private List<Direction> directions;
    private List<Vector3f> lines;
    private boolean isDying = false;

    public ParticleVex(Entity entity) { //getPosition
        super((ClientLevel) entity.level(), entity.getX() + entity.level().getRandom().nextDouble() - 0.5, entity.getY() + 1 + entity.level().getRandom().nextDouble() - 0.5, entity.getZ() + entity.level().getRandom().nextDouble() - 0.5);
        this.entity = entity;
        directions = new ArrayList<>();
        Direction prev = Direction.NORTH;
        directions.add(0, prev);
        for (int i = 1; i < 50; i++) {
            prev = directions.get(i - 1);
            directions.add(i, level.getRandom().nextDouble() < 0.05 ? getRandomFacing(level.getRandom(), prev) : prev);
        }
        calculateLines();
        this.lifetime = 500;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.entity.position().distanceToSqr(new Vec3(x, y, z)) > 10) {
            isDying = true;
        }
        if (!isDying && !this.removed) {
            directions.add(0, level.getRandom().nextDouble() < 0.05 ? getRandomFacing(level.getRandom(), directions.get(0)) : directions.get(0));
            directions.remove(50);
            Vec3 directionVector = new Vec3(directions.get(0).getUnitVec3i().getX(), directions.get(0).getUnitVec3i().getY(), directions.get(0).getUnitVec3i().getZ()).scale(0.01);
            this.setPos(x - directionVector.x, y - directionVector.y, z - directionVector.z);
            calculateLines();
        } else {
            directions.remove(directions.size() - 1);
            calculateLines();
            if (directions.isEmpty()) this.remove();
        }
    }

    @Override
    public ParticleRenderType getGroup() {
        return RENDER;
    }

    private boolean isHidden() {
        return entity instanceof LocalPlayer && Minecraft.getInstance().player.getUUID().equals(entity.getUUID()) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON && this.entity.position().add(0, 1, 0).distanceToSqr(new Vec3(x, y, z)) < 3;
    }

    public static class Group extends ParticleGroup<ParticleVex> {

        public Group(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
            Vec3 cameraPos = camera.position();
            List<List<Vector3f>> strips = new ArrayList<>();
            for (ParticleVex particle : this.particles) {
                if (particle.isHidden() || particle.lines.size() < 2) continue;
                List<Vector3f> strip = new ArrayList<>();
                for (Vector3f line : particle.lines) {
                    strip.add(new Vector3f((float) (line.x - cameraPos.x), (float) (line.y - cameraPos.y), (float) (line.z - cameraPos.z)));
                }
                strips.add(strip);
            }
            return new State(strips);
        }
    }

    private record State(List<List<Vector3f>> strips) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
            if (strips.isEmpty()) return;
            collector.submitCustomGeometry(new PoseStack(), RenderTypes.lines(), (pose, buffer) -> {
                Vector3f normal = new Vector3f();
                for (List<Vector3f> strip : strips) {
                    // 1.21 drew a DEBUG_LINE_STRIP, the lines render type takes segments
                    for (int i = 1; i < strip.size(); i++) {
                        Vector3f from = strip.get(i - 1);
                        Vector3f to = strip.get(i);
                        to.sub(from, normal);
                        if (normal.lengthSquared() == 0) continue;
                        normal.normalize();
                        buffer.addVertex(pose, from).setColor(-1).setNormal(pose, normal).setLineWidth(1.5F);
                        buffer.addVertex(pose, to).setColor(-1).setNormal(pose, normal).setLineWidth(1.5F);
                    }
                }
            });
        }
    }

    private Direction getRandomFacing(RandomSource random, Direction opposite) {
        Direction facing = Direction.getRandom(random); //random
        while (facing.getOpposite().equals(opposite)) facing = Direction.getRandom(random);
        return facing;
    }

    private void calculateLines() {
        lines = new ArrayList<>();
        if (directions.size() == 0) return;
        Direction prev = directions.get(0);
        int currentPosition = 0;
        Vector3f prevBlockPos = new Vector3f((float) x, (float) y, (float) z);
        lines.add(prevBlockPos);
        for (int i = 1; i < directions.size(); i++) {
            if (!directions.get(i).equals(prev) || i == directions.size() - 1) {
                Vec3 directionVector = new Vec3(prev.getUnitVec3i().getX(), prev.getUnitVec3i().getY(), prev.getUnitVec3i().getZ()).scale(0.01);
                Vector3f endBlockPos = new Vector3f((float) (prevBlockPos.x + directionVector.x * (i - currentPosition)), (float) (prevBlockPos.y + directionVector.y * (i - currentPosition)), (float) (prevBlockPos.z + directionVector.z * (i - currentPosition)));
                lines.add(endBlockPos);
                prev = directions.get(i);
                currentPosition = i;
                prevBlockPos = endBlockPos;
            }
        }
    }
}
