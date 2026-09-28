package com.jones.watermelonmod.client.sonar;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Classical Doppler-shift math applied to a pinged entity's real velocity. */
public final class DopplerMath {
    /** Sonar "wave speed" in blocks/tick. Tuned well above normal mob speeds so the formula never inverts, and slow enough to give a readable echo-timing stagger at sonar range. */
    public static final float WAVE_SPEED = 6.0F;

    private DopplerMath() {
    }

    /** Positive = entity closing distance on the listener (approaching), negative = receding. */
    public static double radialVelocity(Entity source, Vec3 listenerPos) {
        Vec3 toListener = listenerPos.subtract(source.position());
        double distance = toListener.length();
        if (distance < 1.0E-4) return 0.0;
        Vec3 unitTowardListener = toListener.scale(1.0 / distance);
        return source.getDeltaMovement().dot(unitTowardListener);
    }

    /** f_observed = f0 * v / (v - v_r); v_r > 0 (approaching) raises the observed frequency. */
    public static float observedFrequency(float sourceFrequency, double radialVelocity) {
        double denominator = WAVE_SPEED - radialVelocity;
        if (Math.abs(denominator) < 1.0E-3) denominator = Math.signum(denominator) * 1.0E-3;
        return (float) (sourceFrequency * WAVE_SPEED / denominator);
    }
}
