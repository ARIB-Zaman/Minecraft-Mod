package com.jones.watermelonmod.signal;

import com.jones.watermelonmod.echofunnel.EchoFunnelBankStore;

import java.util.List;

/**
 * Whether a signal's energy leans toward the Echo Funnel's higher or lower
 * frequency banks — the "Resonance Gamble": a beam worth catching, or one
 * that's safer to let go.
 */
public final class SignalClassifier {
    public enum Tier {
        /** Dominant energy in the two higher-frequency banks. */
        BENEFICIAL,
        /** Dominant energy in the two lower-frequency banks. */
        DEADLY
    }

    private SignalClassifier() {
    }

    public static Tier classify(SonicSignal signal) {
        List<Double> magnitudes = DiscreteFourierTransform.magnitudes(signal);
        double[] bankEnergy = new double[EchoFunnelBankStore.BANK_COUNT];
        for (int bin = 0; bin < magnitudes.size(); bin++) {
            bankEnergy[EchoFunnelBankStore.bankForBin(bin)] += magnitudes.get(bin);
        }
        int dominantBank = 0;
        for (int bank = 1; bank < bankEnergy.length; bank++) {
            if (bankEnergy[bank] > bankEnergy[dominantBank]) {
                dominantBank = bank;
            }
        }
        return dominantBank >= EchoFunnelBankStore.BANK_COUNT / 2 ? Tier.BENEFICIAL : Tier.DEADLY;
    }
}
