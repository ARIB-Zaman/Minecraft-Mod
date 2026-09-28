package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Immutable definition of a boss encounter. Entries are ordered from highest
 * to lowest health threshold, allowing the evaluator to use the first match.
 */
public record BossProfile(Identifier id, List<BossPhase> phases) {
    public BossProfile {
        phases = List.copyOf(phases);
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("A boss profile must contain at least one phase");
        }
        validateThresholdOrder(phases.stream().map(BossPhase::startsAtHealthFraction).toList());
        phases.forEach(phase -> validateThresholdOrder(phase.subphases().stream().map(BossSubphase::startsAtHealthFraction).toList()));
    }

    public BossPhase initialPhase() {
        return phases.getFirst();
    }

    public BossState stateAtHealthFraction(double healthFraction) {
        double clampedHealth = Math.clamp(healthFraction, 0.0, 1.0);
        BossPhase phase = phases.stream()
                .filter(candidate -> clampedHealth <= candidate.startsAtHealthFraction())
                .findFirst()
                .orElseThrow();
        BossSubphase subphase = phase.subphases().stream()
                .filter(candidate -> clampedHealth <= candidate.startsAtHealthFraction())
                .findFirst()
                .orElseThrow();
        return new BossState(id, phase.id(), subphase.id());
    }

    public BossSubphase subphase(BossState state) {
        return phases.stream()
                .filter(phase -> phase.id().equals(state.phaseId()))
                .flatMap(phase -> phase.subphases().stream())
                .filter(subphase -> subphase.id().equals(state.subphaseId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("State does not belong to profile " + id));
    }

    private static void validateThresholdOrder(List<Double> thresholds) {
        if (thresholds.getFirst() != 1.0) {
            throw new IllegalArgumentException("The first phase and subphase must start at full health (1.0)");
        }
        for (int index = 1; index < thresholds.size(); index++) {
            if (thresholds.get(index) >= thresholds.get(index - 1)) {
                throw new IllegalArgumentException("Health thresholds must be strictly descending");
            }
        }
    }
}
