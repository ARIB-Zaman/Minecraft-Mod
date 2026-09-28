package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server tells the player it just hit with a signal-bearing attack to start a
 * Veil recompose trial: {@code seconds} is the recovery window, shorter as
 * the attack's source gets weaker.
 */
public record VeilBlindPayload(int seconds) implements CustomPacketPayload {
    public static final Type<VeilBlindPayload> TYPE = new Type<>(WatermelonMod.id("veil_blind"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilBlindPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VeilBlindPayload::seconds,
            VeilBlindPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
