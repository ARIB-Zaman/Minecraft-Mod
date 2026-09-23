package com.jones.watermelonmod.signal;

import net.minecraft.util.RandomSource;

/** Default generator: copies the configured components and assigns fresh noise. */
public final class CompoundSignalGenerator implements SignalGenerator {
    @Override
    public SonicSignal generate(SonicSignalTemplate template, RandomSource random) {
        return new SonicSignal(1, template.sampleRateHz(), template.durationTicks(), template.components(), template.noise(), random.nextLong());
    }
}
