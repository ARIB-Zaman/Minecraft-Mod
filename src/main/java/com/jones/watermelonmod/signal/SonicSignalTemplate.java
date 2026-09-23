package com.jones.watermelonmod.signal;

/** Tuning recipe for a 64-bin Sonic Radiation spectrum. */
public record SonicSignalTemplate(double noiseFloor, double noiseVariation, double spikeMinimum, double spikeMaximum) {
    public SonicSignalTemplate {
        if (noiseFloor < 0.0 || noiseVariation < 0.0 || spikeMinimum <= 0.0 || spikeMaximum < spikeMinimum) {
            throw new IllegalArgumentException("Invalid discrete Sonic Signal spectrum tuning");
        }
    }
}
