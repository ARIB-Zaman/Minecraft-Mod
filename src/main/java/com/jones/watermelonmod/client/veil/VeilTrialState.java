package com.jones.watermelonmod.client.veil;

/**
 * A short, timed test of resolving one blur parameter (angle) under time
 * pressure.
 */
public final class VeilTrialState {
    /** What started this trial, so success/failure can react appropriately. */
    public enum TriggerKind {
        /** The standalone {@code /veil trial} test command. */
        MANUAL,
        /** A landed Sonic Radiation hit during normal combat. */
        NORMAL_HIT,
        /** The Warden's telegraphed Convergence attack at a health threshold. */
        CONVERGENCE
    }

    private static boolean active;
    private static long endTimeMillis;
    private static float totalSeconds = 1.0F;
    private static float targetAngle;
    private static TriggerKind triggerKind = TriggerKind.MANUAL;

    private VeilTrialState() {
    }

    public static void start(float targetAngle, int seconds, TriggerKind triggerKind) {
        VeilTrialState.targetAngle = targetAngle;
        VeilTrialState.triggerKind = triggerKind;
        VeilTrialState.totalSeconds = Math.max(1, seconds);
        endTimeMillis = System.currentTimeMillis() + seconds * 1000L;
        active = true;
    }

    /** Fraction of the window still remaining, from 1.0 (just started) down to 0.0 (out of time). */
    public static float fractionRemaining() {
        return Math.clamp(secondsRemaining() / totalSeconds, 0.0F, 1.0F);
    }

    public static TriggerKind triggerKind() {
        return triggerKind;
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
