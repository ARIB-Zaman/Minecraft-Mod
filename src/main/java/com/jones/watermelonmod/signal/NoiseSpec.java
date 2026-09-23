package com.jones.watermelonmod.signal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Configures deterministic white noise added to a compound signal. */
public record NoiseSpec(double amplitude) {
    public static final Codec<NoiseSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("amplitude").forGetter(NoiseSpec::amplitude)
    ).apply(instance, NoiseSpec::new));

    public NoiseSpec {
        if (amplitude < 0.0) {
            throw new IllegalArgumentException("Noise amplitude cannot be negative");
        }
    }
}
