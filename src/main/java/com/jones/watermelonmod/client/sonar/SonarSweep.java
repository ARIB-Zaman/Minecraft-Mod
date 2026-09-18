package com.jones.watermelonmod.client.sonar;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Fires a real ray-cast sweep from the player's eye and returns one {@link SonarBlip} per hit —
 * a wall surface (colored by the block's actual map color, so different materials genuinely look
 * different) or a nearby mob (colored by hostility and brightness-shifted by a real Doppler ratio
 * computed from the mob's current velocity). Each blip's reveal tick is derived from a real
 * round-trip travel-time calculation, not a cosmetic sort.
 */
public final class SonarSweep {
    public static final double RANGE = 24.0;
    private static final int RAYS_PER_RING = 48;
    private static final float[] PITCH_RINGS = {-25.0F, 0.0F, 25.0F};

    private SonarSweep() {
    }

    public static List<SonarBlip> fire(Level level, LocalPlayer player, int fireTick) {
        List<SonarBlip> blips = new ArrayList<>();
        Vec3 eye = player.getEyePosition();

        for (float pitch : PITCH_RINGS) {
            for (int i = 0; i < RAYS_PER_RING; i++) {
                float yaw = i * (360.0F / RAYS_PER_RING);
                Vec3 direction = directionFromYawPitch(yaw, pitch);
                Vec3 to = eye.add(direction.scale(RANGE));
                BlockHitResult hit = level.clip(new ClipContext(eye, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    double distance = eye.distanceTo(hit.getLocation());
                    BlockState state = level.getBlockState(hit.getBlockPos());
                    int color = ARGB.opaque(state.getMapColor(level, hit.getBlockPos()).col);
                    blips.add(new SonarBlip(hit.getLocation(), color, revealTick(fireTick, distance), false, 1.0F));
                }
            }
        }

        AABB searchBox = new AABB(eye, eye).inflate(RANGE);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, searchBox, Mob::isAlive)) {
            double distance = eye.distanceTo(mob.position());
            if (distance > RANGE) continue;
            double radialVelocity = DopplerMath.radialVelocity(mob, eye);
            float dopplerRatio = DopplerMath.observedFrequency(1.0F, radialVelocity);
            boolean hostile = mob instanceof Enemy;
            int baseColor = hostile ? 0xFFFF4040 : 0xFF40C0FF;
            int color = ARGB.scaleRGB(baseColor, Mth.clamp(dopplerRatio, 0.5F, 1.5F));
            blips.add(new SonarBlip(mob.position(), color, revealTick(fireTick, distance), true, dopplerRatio));
        }

        return blips;
    }

    private static int revealTick(int fireTick, double distance) {
        return fireTick + (int) Math.round(2.0 * distance / DopplerMath.WAVE_SPEED);
    }

    private static Vec3 directionFromYawPitch(float yawDeg, float pitchDeg) {
        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);
        double horizontal = Math.cos(pitchRad);
        return new Vec3(-Math.sin(yawRad) * horizontal, -Math.sin(pitchRad), Math.cos(yawRad) * horizontal);
    }
}
