package com.jones.watermelonmod.signal;

/** Configures deterministic white noise added to a compound signal. */
public record NoiseSpec(double amplitude) {
    public NoiseSpec {
        if (amplitude < 0.0) {
            throw new IllegalArgumentException("Noise amplitude cannot be negative");
        }
    }
}
