package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.signal.SonicSignal;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.stream.Stream;

/** Persistent, bounded signal inventory owned by one Echo Funnel stack. */
public record EchoFunnelCaptureStore(List<CapturedSignal> captures) {
    public static final int CAPACITY = 3;
    private static final Codec<List<CapturedSignal>> CAPTURE_CODEC = CapturedSignal.CODEC.listOf();
    /** Also reads the previous direct signal list used during development. */
    public static final Codec<EchoFunnelCaptureStore> CODEC = Codec.either(CAPTURE_CODEC, SonicSignal.CODEC.listOf()).xmap(
            decoded -> decoded.map(EchoFunnelCaptureStore::new,
                    legacy -> new EchoFunnelCaptureStore(legacy.stream().map(signal -> new CapturedSignal(signal, List.of())).toList())),
            store -> Either.left(store.captures())
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoFunnelCaptureStore> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public EchoFunnelCaptureStore {
        captures = List.copyOf(captures);
        if (captures.size() > CAPACITY) {
            throw new IllegalArgumentException("Echo Funnel may store at most " + CAPACITY + " signals");
        }
    }

    public static EchoFunnelCaptureStore empty() {
        return new EchoFunnelCaptureStore(List.of());
    }

    public boolean hasCapacity() {
        return captures.size() < CAPACITY;
    }

    public EchoFunnelCaptureStore capture(SonicSignal signal) {
        if (!hasCapacity()) {
            throw new IllegalStateException("Echo Funnel signal storage is full");
        }
        return new EchoFunnelCaptureStore(Stream.concat(captures.stream(), Stream.of(new CapturedSignal(signal, List.of()))).toList());
    }

    public List<SonicSignal> signals() {
        return captures.stream().map(CapturedSignal::signal).toList();
    }

    public CapturedSignal captureAt(int slot) {
        return captures.get(slot);
    }

    public EchoFunnelCaptureStore remove(int slot) {
        if (slot < 0 || slot >= captures.size()) {
            throw new IndexOutOfBoundsException("Echo Funnel signal slot: " + slot);
        }
        return new EchoFunnelCaptureStore(java.util.stream.IntStream.range(0, captures.size())
                .filter(index -> index != slot)
                .mapToObj(captures::get)
                .toList());
    }

    public EchoFunnelCaptureStore replace(int slot, CapturedSignal capture) {
        if (slot < 0 || slot >= captures.size()) {
            throw new IndexOutOfBoundsException("Echo Funnel signal slot: " + slot);
        }
        return new EchoFunnelCaptureStore(java.util.stream.IntStream.range(0, captures.size())
                .mapToObj(index -> index == slot ? capture : captures.get(index))
                .toList());
    }
}
