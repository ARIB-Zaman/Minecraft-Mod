package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

import java.util.List;

/** A health-defined boss phase containing one or more subphases. */
public record BossPhase(Identifier id, double startsAtHealthFraction, List<BossSubphase> subphases) {
    public BossPhase {
        if (startsAtHealthFraction <= 0.0 || startsAtHealthFraction > 1.0) {
            throw new IllegalArgumentException("Health thresholds must be within (0, 1]");
        }
        subphases = List.copyOf(subphases);
        if (subphases.isEmpty()) {
            throw new IllegalArgumentException("A boss phase must contain at least one subphase");
        }
    }
}
