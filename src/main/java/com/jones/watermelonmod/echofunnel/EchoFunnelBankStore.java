package com.jones.watermelonmod.echofunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/** Persistent, four-band frequency storage owned by an Echo Funnel stack. */
public record EchoFunnelBankStore(List<List<BankedFrequency>> banks) {
    public static final int BANK_COUNT = 4;
    public static final double CAPACITY = 1.0;
    public static final int POINT_CAPACITY = 20;
    public static final Codec<EchoFunnelBankStore> CODEC = BankedFrequency.CODEC.listOf().listOf().comapFlatMap(
            banks -> banks.size() == BANK_COUNT && banks.stream().allMatch(EchoFunnelBankStore::isValidBank)
                    ? DataResult.success(new EchoFunnelBankStore(banks))
                    : DataResult.error(() -> "Echo Funnel requires four frequency banks within capacity"),
            EchoFunnelBankStore::banks
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoFunnelBankStore> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public EchoFunnelBankStore {
        banks = banks.stream().map(List::copyOf).toList();
        if (banks.size() != BANK_COUNT || banks.stream().anyMatch(bank -> !isValidBank(bank))) {
            throw new IllegalArgumentException("Echo Funnel requires four frequency banks within capacity");
        }
    }

    public static EchoFunnelBankStore empty() {
        return new EchoFunnelBankStore(List.of(List.of(), List.of(), List.of(), List.of()));
    }

    /** Maps a normal DFT bin to its symmetric low-to-high frequency bank. */
    public static int bankForBin(int bin) {
        if (bin < 0 || bin >= 64) {
            throw new IllegalArgumentException("FFT bin outside 64-point spectrum: " + bin);
        }
        int signedFrequency = bin <= 32 ? bin : bin - 64;
        return Math.min(BANK_COUNT - 1, Math.abs(signedFrequency) / 8);
    }

    public double fill(int bank) {
        return banks.get(bank).stream().mapToDouble(BankedFrequency::normalizedAmplitude).sum();
    }

    public int points(int bank) {
        return (int) Math.floor(fill(bank) * POINT_CAPACITY + 1.0E-9);
    }

    public boolean canAfford(List<Integer> costs) {
        return costs.size() == BANK_COUNT && java.util.stream.IntStream.range(0, BANK_COUNT).allMatch(bank -> points(bank) >= costs.get(bank));
    }

    /** Deducts an affordable point cost while preserving the stored-bin history. */
    public EchoFunnelBankStore withdraw(List<Integer> costs) {
        if (!canAfford(costs)) {
            throw new IllegalStateException("Insufficient Echo Funnel bank points");
        }
        List<List<BankedFrequency>> updated = new java.util.ArrayList<>(BANK_COUNT);
        for (int bank = 0; bank < BANK_COUNT; bank++) {
            double remaining = costs.get(bank) / (double) POINT_CAPACITY;
            List<BankedFrequency> updatedBank = new java.util.ArrayList<>();
            for (BankedFrequency frequency : banks.get(bank)) {
                double retained = Math.max(0.0, frequency.normalizedAmplitude() - remaining);
                remaining = Math.max(0.0, remaining - frequency.normalizedAmplitude());
                if (retained > 1.0E-9) {
                    updatedBank.add(new BankedFrequency(frequency.bin(), retained));
                }
            }
            updated.add(updatedBank);
        }
        return new EchoFunnelBankStore(updated);
    }

    /** Adds the available portion of a frequency; overflow simply fills the bank. */
    public EchoFunnelBankStore store(BankedFrequency frequency) {
        int bank = bankForBin(frequency.bin());
        double available = Math.max(0.0, CAPACITY - fill(bank));
        double deposited = Math.min(available, frequency.normalizedAmplitude());
        if (deposited == 0.0) {
            return this;
        }
        List<List<BankedFrequency>> updated = new java.util.ArrayList<>(banks.stream().map(java.util.ArrayList::new).toList());
        updated.get(bank).add(new BankedFrequency(frequency.bin(), deposited));
        return new EchoFunnelBankStore(updated);
    }

    private static boolean isValidBank(List<BankedFrequency> bank) {
        return bank.stream().mapToDouble(BankedFrequency::normalizedAmplitude).sum() <= CAPACITY + 1.0E-9;
    }
}
