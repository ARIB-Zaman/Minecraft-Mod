package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

/** A health-defined tuning tier inside a boss phase. */
public record BossSubphase(Identifier id, double startsAtHealthFraction, BossDarkness darkness, List<Identifier> attackIds) {
    public BossSubphase {
        validateThreshold(startsAtHealthFraction);
        Objects.requireNonNull(darkness, "darkness cannot be null");
        attackIds = List.copyOf(attackIds);
    }

    public BossSubphase(Identifier id, double startsAtHealthFraction, List<Identifier> attackIds) {
        this(id, startsAtHealthFraction, BossDarkness.none(), attackIds);
    }

    private static void validateThreshold(double threshold) {
        if (threshold <= 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Health thresholds must be within (0, 1]");
        }
    }
}
