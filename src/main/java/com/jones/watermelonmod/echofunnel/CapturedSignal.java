package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.signal.SonicSignal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** A captured signal plus its server-authoritative, already-banked bins. */
public record CapturedSignal(SonicSignal signal, List<Integer> bankedBins) {
    public static final int MAX_BANKED_BINS = 3;
    public static final Codec<CapturedSignal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SonicSignal.CODEC.fieldOf("signal").forGetter(CapturedSignal::signal),
            Codec.INT.listOf().optionalFieldOf("banked_bins", List.of()).forGetter(CapturedSignal::bankedBins)
    ).apply(instance, CapturedSignal::new));

    public CapturedSignal {
        bankedBins = List.copyOf(bankedBins);
        if (bankedBins.size() > MAX_BANKED_BINS || bankedBins.stream().distinct().count() != bankedBins.size()
                || bankedBins.stream().anyMatch(bin -> bin < 0 || bin >= SonicSignal.FFT_SIZE)) {
            throw new IllegalArgumentException("Invalid banked-bin history");
        }
    }

    public boolean canBank(int bin) {
        return bankedBins.size() < MAX_BANKED_BINS && !bankedBins.contains(bin);
    }

    public CapturedSignal bank(int bin) {
        if (!canBank(bin)) {
            throw new IllegalStateException("Frequency was already banked or signal is exhausted");
        }
        return new CapturedSignal(signal, java.util.stream.Stream.concat(bankedBins.stream(), java.util.stream.Stream.of(bin)).toList());
    }

    public boolean isExhausted() {
        return bankedBins.size() == MAX_BANKED_BINS;
    }
}
