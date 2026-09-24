package com.jones.watermelonmod.echofunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** One extracted FFT bin, stored as a normalized contribution to its bank. */
public record BankedFrequency(int bin, double normalizedAmplitude) {
    public static final Codec<BankedFrequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("bin").forGetter(BankedFrequency::bin),
            Codec.DOUBLE.fieldOf("normalized_amplitude").forGetter(BankedFrequency::normalizedAmplitude)
    ).apply(instance, BankedFrequency::new));

    public BankedFrequency {
        if (bin < 0 || bin >= 64 || !Double.isFinite(normalizedAmplitude) || normalizedAmplitude <= 0.0 || normalizedAmplitude > 1.0) {
            throw new IllegalArgumentException("Invalid banked frequency");
        }
    }
}
