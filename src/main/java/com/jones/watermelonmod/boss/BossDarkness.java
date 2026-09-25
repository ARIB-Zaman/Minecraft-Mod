package com.jones.watermelonmod.boss;

/**
 * Defines the darkness effect behavior associated with a boss phase or subphase.
 */
public record BossDarkness(DarknessMode mode, int durationTicks, int refreshIntervalTicks, double radius) {
    public enum DarknessMode {
        NONE,
        PULSE,
        CONTINUOUS
    }

    public static final BossDarkness NONE = new BossDarkness(DarknessMode.NONE, 0, 0, 0.0);

    public static BossDarkness none() {
        return NONE;
    }

    public static BossDarkness pulse(int durationTicks, double radius) {
        return new BossDarkness(DarknessMode.PULSE, durationTicks, 0, radius);
    }

    public static BossDarkness continuous(int refreshIntervalTicks, int durationTicks, double radius) {
        return new BossDarkness(DarknessMode.CONTINUOUS, durationTicks, refreshIntervalTicks, radius);
    }
}
