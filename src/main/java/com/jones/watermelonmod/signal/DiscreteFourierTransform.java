package com.jones.watermelonmod.signal;

import java.util.ArrayList;
import java.util.List;

/** Small, explicit 64-point DFT implementation shared by generation and UI restoration. */
public final class DiscreteFourierTransform {
    private DiscreteFourierTransform() {
    }

    public static List<Double> inverseReal(double[] real, double[] imaginary) {
        requireSize(real, imaginary);
        List<Double> samples = new ArrayList<>(SonicSignal.FFT_SIZE);
        for (int time = 0; time < SonicSignal.FFT_SIZE; time++) {
            double sum = 0.0;
            for (int frequency = 0; frequency < SonicSignal.FFT_SIZE; frequency++) {
                double angle = 2.0 * Math.PI * frequency * time / SonicSignal.FFT_SIZE;
                sum += real[frequency] * Math.cos(angle) - imaginary[frequency] * Math.sin(angle);
            }
            samples.add(sum / SonicSignal.FFT_SIZE);
        }
        return List.copyOf(samples);
    }

    /** Returns the magnitude for all 64 discrete frequency bins. */
    public static List<Double> magnitudes(SonicSignal signal) {
        List<Double> magnitudes = new ArrayList<>(SonicSignal.FFT_SIZE);
        for (int frequency = 0; frequency < SonicSignal.FFT_SIZE; frequency++) {
            double real = 0.0;
            double imaginary = 0.0;
            for (int time = 0; time < SonicSignal.FFT_SIZE; time++) {
                double angle = 2.0 * Math.PI * frequency * time / SonicSignal.FFT_SIZE;
                double sample = signal.sampleAt(time);
                real += sample * Math.cos(angle);
                imaginary -= sample * Math.sin(angle);
            }
            magnitudes.add(Math.hypot(real, imaginary));
        }
        return List.copyOf(magnitudes);
    }

    private static void requireSize(double[] real, double[] imaginary) {
        if (real.length != SonicSignal.FFT_SIZE || imaginary.length != SonicSignal.FFT_SIZE) {
            throw new IllegalArgumentException("DFT requires exactly 64 bins");
        }
    }
}
