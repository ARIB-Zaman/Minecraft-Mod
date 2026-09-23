package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.signal.SonicSignal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.stream.Stream;

/** Persistent, bounded signal inventory owned by one Echo Funnel stack. */
public record EchoFunnelCaptureStore(List<SonicSignal> signals) {
    public static final int CAPACITY = 3;
    public static final Codec<EchoFunnelCaptureStore> CODEC = SonicSignal.CODEC.listOf().comapFlatMap(
            signals -> signals.size() <= CAPACITY
                    ? DataResult.success(new EchoFunnelCaptureStore(signals))
                    : DataResult.error(() -> "Echo Funnel may store at most " + CAPACITY + " signals"),
            EchoFunnelCaptureStore::signals
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoFunnelCaptureStore> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public EchoFunnelCaptureStore {
        signals = List.copyOf(signals);
        if (signals.size() > CAPACITY) {
            throw new IllegalArgumentException("Echo Funnel may store at most " + CAPACITY + " signals");
        }
    }

    public static EchoFunnelCaptureStore empty() {
        return new EchoFunnelCaptureStore(List.of());
    }

    public boolean hasCapacity() {
        return signals.size() < CAPACITY;
    }

    public EchoFunnelCaptureStore capture(SonicSignal signal) {
        if (!hasCapacity()) {
            throw new IllegalStateException("Echo Funnel signal storage is full");
        }
        return new EchoFunnelCaptureStore(Stream.concat(signals.stream(), Stream.of(signal)).toList());
    }
}
