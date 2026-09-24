package com.jones.watermelonmod.signal;

import net.minecraft.util.RandomSource;

import java.util.HashSet;
import java.util.Set;

/** Creates a noisy 64-bin spectrum, adds 2-6 peaks, then inverse-transforms it. */
public final class CompoundSignalGenerator implements SignalGenerator {
    private static final int FIRST_COMPLEX_BIN = 1;
    private static final int LAST_COMPLEX_BIN = SonicSignal.FFT_SIZE / 2 - 1;

    @Override
    public SonicSignal generate(SonicSignalTemplate template, RandomSource random) {
        double[] real = new double[SonicSignal.FFT_SIZE];
        double[] imaginary = new double[SonicSignal.FFT_SIZE];

        // Conjugate symmetry guarantees a real-valued periodic signal after IFFT.
        for (int bin = FIRST_COMPLEX_BIN; bin <= LAST_COMPLEX_BIN; bin++) {
            addBin(real, imaginary, bin, template.noiseFloor() + random.nextDouble() * template.noiseVariation(), random);
        }

        int spikeCount = 2 + random.nextInt(5);
        Set<Integer> spikeBins = new HashSet<>();
        while (spikeBins.size() < spikeCount) {
            spikeBins.add(FIRST_COMPLEX_BIN + random.nextInt(LAST_COMPLEX_BIN));
        }
        for (int bin : spikeBins) {
            addBin(real, imaginary, bin, template.spikeMinimum() + random.nextDouble() * (template.spikeMaximum() - template.spikeMinimum()), random);
        }

        return SonicSignal.ofSamples(DiscreteFourierTransform.inverseReal(real, imaginary));
    }

    private static void addBin(double[] real, double[] imaginary, int bin, double magnitude, RandomSource random) {
        double phase = random.nextDouble() * 2.0 * Math.PI;
        double realPart = magnitude * Math.cos(phase);
        double imaginaryPart = magnitude * Math.sin(phase);
        int mirror = SonicSignal.FFT_SIZE - bin;
        real[bin] += realPart;
        imaginary[bin] += imaginaryPart;
        real[mirror] += realPart;
        imaginary[mirror] -= imaginaryPart;
    }
}
