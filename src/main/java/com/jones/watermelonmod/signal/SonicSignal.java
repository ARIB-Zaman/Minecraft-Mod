package com.jones.watermelonmod.signal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * A captured periodic signal represented exclusively by one period of 64
 * discrete time-domain samples. Its spectrum is restored with a 64-point DFT.
 */
public record SonicSignal(int formatVersion, List<Double> samples) {
    public static final int FFT_SIZE = 64;
    public static final int FORMAT_VERSION = 2;

    /** Optional legacy fields allow existing development captures to load once. */
    public static final Codec<SonicSignal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("format_version").forGetter(SonicSignal::formatVersion),
            Codec.DOUBLE.listOf().optionalFieldOf("samples", List.of()).forGetter(SonicSignal::samples),
            Codec.INT.optionalFieldOf("sample_rate_hz", 256).forGetter(signal -> 256),
            Codec.INT.optionalFieldOf("duration_ticks", 60).forGetter(signal -> 60),
            SignalComponent.CODEC.listOf().optionalFieldOf("components", List.of()).forGetter(signal -> List.of()),
            NoiseSpec.CODEC.optionalFieldOf("noise", new NoiseSpec(0.0)).forGetter(signal -> new NoiseSpec(0.0)),
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(signal -> 0L)
    ).apply(instance, SonicSignal::decode));
    public static final StreamCodec<RegistryFriendlyByteBuf, SonicSignal> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public SonicSignal {
        if (formatVersion != FORMAT_VERSION || samples.size() != FFT_SIZE || samples.stream().anyMatch(value -> !Double.isFinite(value))) {
            throw new IllegalArgumentException("Sonic Signals must contain exactly 64 finite discrete samples");
        }
        samples = List.copyOf(samples);
    }

    public static SonicSignal ofSamples(List<Double> samples) {
        return new SonicSignal(FORMAT_VERSION, samples);
    }

    private static SonicSignal decode(int formatVersion, List<Double> samples, int sampleRateHz, int durationTicks,
                                      List<SignalComponent> components, NoiseSpec noise, long seed) {
        if (formatVersion == FORMAT_VERSION) {
            return new SonicSignal(formatVersion, samples);
        }
        if (formatVersion == 1) {
            return ofSamples(java.util.stream.IntStream.range(0, FFT_SIZE)
                    .mapToDouble(index -> legacySample(index, sampleRateHz, durationTicks, components, noise, seed))
                    .boxed().toList());
        }
        throw new IllegalArgumentException("Unsupported Sonic Signal format: " + formatVersion);
    }

    private static double legacySample(int index, int sampleRateHz, int durationTicks, List<SignalComponent> components, NoiseSpec noise, long seed) {
        int legacySampleCount = Math.max(1, Math.toIntExact((long) sampleRateHz * durationTicks / 20L));
        int legacyIndex = index * legacySampleCount / FFT_SIZE;
        double timeSeconds = legacyIndex / (double) sampleRateHz;
        double compound = components.stream().mapToDouble(component -> component.amplitude()
                * Math.sin(2.0 * Math.PI * component.frequencyHz() * timeSeconds + component.phaseRadians())).sum();
        return compound + noise.amplitude() * deterministicWhiteNoise(seed, legacyIndex);
    }

    private static double deterministicWhiteNoise(long seed, int index) {
        long mixed = seed + 0x9E3779B97F4A7C15L * index;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        return ((mixed >>> 11) * 0x1.0p-53) * 2.0 - 1.0;
    }

    public int sampleCount() {
        return FFT_SIZE;
    }

    public double sampleAt(int index) {
        if (index < 0 || index >= FFT_SIZE) {
            throw new IndexOutOfBoundsException("Signal sample index: " + index);
        }
        return samples.get(index);
    }

    public double periodicSampleAt(int index) {
        return samples.get(Math.floorMod(index, FFT_SIZE));
    }
}
