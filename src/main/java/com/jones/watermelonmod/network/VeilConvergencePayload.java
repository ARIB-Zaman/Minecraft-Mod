package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server tells the target of the Warden's telegraphed Convergence attack to
 * start a heavier, tighter recompose trial — the fight's signature moment at
 * the 50%/25% health thresholds.
 */
public record VeilConvergencePayload(int seconds, float size) implements CustomPacketPayload {
    public static final Type<VeilConvergencePayload> TYPE = new Type<>(WatermelonMod.id("veil_convergence"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilConvergencePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VeilConvergencePayload::seconds,
            ByteBufCodecs.FLOAT,
            VeilConvergencePayload::size,
            VeilConvergencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
