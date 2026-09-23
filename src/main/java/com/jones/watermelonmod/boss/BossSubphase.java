package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

import java.util.List;

/** A health-defined tuning tier inside a boss phase. */
public record BossSubphase(Identifier id, double startsAtHealthFraction, List<Identifier> attackIds) {
    public BossSubphase {
        validateThreshold(startsAtHealthFraction);
        attackIds = List.copyOf(attackIds);
    }

    private static void validateThreshold(double threshold) {
        if (threshold <= 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Health thresholds must be within (0, 1]");
        }
    }
}
