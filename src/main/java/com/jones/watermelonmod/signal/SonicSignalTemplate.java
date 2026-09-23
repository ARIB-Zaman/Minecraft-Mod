package com.jones.watermelonmod.signal;

import java.util.List;

/** Tunable recipe used to create a unique deterministic signal per attack. */
public record SonicSignalTemplate(int sampleRateHz, int durationTicks, List<SignalComponent> components, NoiseSpec noise) {
    public SonicSignalTemplate {
        if (sampleRateHz <= 0 || durationTicks <= 0) {
            throw new IllegalArgumentException("Signal sample rate and duration must be positive");
        }
        components = List.copyOf(components);
        if (components.isEmpty()) {
            throw new IllegalArgumentException("A Sonic Radiation signal needs at least one component");
        }
    }
}
