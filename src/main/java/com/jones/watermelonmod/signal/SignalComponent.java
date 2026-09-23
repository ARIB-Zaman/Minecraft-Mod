package com.jones.watermelonmod.signal;

/** One sinusoidal component of a generated Sonic Radiation signal. */
public record SignalComponent(double frequencyHz, double amplitude, double phaseRadians) {
    public SignalComponent {
        if (frequencyHz < 0.0 || amplitude < 0.0) {
            throw new IllegalArgumentException("Signal frequency and amplitude cannot be negative");
        }
    }
}
