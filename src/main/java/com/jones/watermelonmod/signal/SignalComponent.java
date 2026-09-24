package com.jones.watermelonmod.signal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One sinusoidal component of a generated Sonic Radiation signal. */
public record SignalComponent(double frequencyHz, double amplitude, double phaseRadians) {
    public static final Codec<SignalComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("frequency_hz").forGetter(SignalComponent::frequencyHz),
            Codec.DOUBLE.fieldOf("amplitude").forGetter(SignalComponent::amplitude),
            Codec.DOUBLE.fieldOf("phase_radians").forGetter(SignalComponent::phaseRadians)
    ).apply(instance, SignalComponent::new));

    public SignalComponent {
        if (frequencyHz < 0.0 || amplitude < 0.0) {
            throw new IllegalArgumentException("Signal frequency and amplitude cannot be negative");
        }
    }
}
