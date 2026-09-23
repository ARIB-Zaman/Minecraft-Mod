package com.jones.watermelonmod.signal;

import java.util.List;

/**
 * Immutable attack payload. The seed makes its noise reproducible, so future
 * DSP code can reconstruct the exact waveform without storing every sample.
 */
public record SonicSignal(int formatVersion, int sampleRateHz, int durationTicks, List<SignalComponent> components, NoiseSpec noise, long seed) {
    public SonicSignal {
        if (formatVersion != 1 || sampleRateHz <= 0 || durationTicks <= 0) {
            throw new IllegalArgumentException("Unsupported or invalid Sonic Signal format");
        }
        components = List.copyOf(components);
    }

    public int sampleCount() {
        return Math.toIntExact((long) sampleRateHz * durationTicks / 20L);
    }

    public double sampleAt(int index) {
        if (index < 0 || index >= sampleCount()) {
            throw new IndexOutOfBoundsException("Signal sample index: " + index);
        }
        double timeSeconds = index / (double) sampleRateHz;
        double compound = components.stream()
                .mapToDouble(component -> component.amplitude()
                        * Math.sin(2.0 * Math.PI * component.frequencyHz() * timeSeconds + component.phaseRadians()))
                .sum();
        return compound + noise.amplitude() * deterministicWhiteNoise(seed, index);
    }

    private static double deterministicWhiteNoise(long seed, int index) {
        long mixed = seed + 0x9E3779B97F4A7C15L * index;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        return ((mixed >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }
}
