package com.jones.watermelonmod.signal;

import net.minecraft.util.RandomSource;

/** Replaceable source of Sonic Radiation signal instances. */
@FunctionalInterface
public interface SignalGenerator {
    SonicSignal generate(SonicSignalTemplate template, RandomSource random);
}
