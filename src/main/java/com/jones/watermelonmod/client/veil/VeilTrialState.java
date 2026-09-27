package com.jones.watermelonmod.client.veil;

/**
 * A short, timed test of resolving one blur parameter (angle) under time
 * pressure. Prototype only — validates whether sneak+scroll tuning feels
 * fair inside a combat-length window, before touching real boss code.
 */
public final class VeilTrialState {
    private static boolean active;
    private static long endTimeMillis;
    private static float targetAngle;

    private VeilTrialState() {
    }

    public static void start(float targetAngle, int seconds) {
        VeilTrialState.targetAngle = targetAngle;
        endTimeMillis = System.currentTimeMillis() + seconds * 1000L;
        active = true;
    }

    public static boolean isActive() {
        return active;
    }

    public static float targetAngle() {
        return targetAngle;
    }

    public static float secondsRemaining() {
        return Math.max(0.0F, (endTimeMillis - System.currentTimeMillis()) / 1000.0F);
    }

    public static boolean expired() {
        return active && System.currentTimeMillis() >= endTimeMillis;
    }

    public static void stop() {
        active = false;
    }

    /** Angular distance to the target, accounting for a motion blur's 180°-periodic direction. */
    public static float distanceTo(float angle) {
        float rawDiff = Math.abs(angle - targetAngle);
        return Math.min(rawDiff, 180.0F - rawDiff);
    }
}
