package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client tells the server it successfully recomposed a combat-triggered
 * trial in time, so the nearby Radiation Warden should be staggered — the
 * reward half of the mechanic, mirrored against {@link VeilBlindPayload}.
 */
public record VeilRecomposedPayload(int angularDistanceDegrees) implements CustomPacketPayload {
    public static final Type<VeilRecomposedPayload> TYPE = new Type<>(WatermelonMod.id("veil_recomposed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilRecomposedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VeilRecomposedPayload::angularDistanceDegrees,
            VeilRecomposedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
