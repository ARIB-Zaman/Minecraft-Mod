package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client request to nudge one DSP parameter of the currently equipped
 * goggles, live, without opening the workbench (sneak + scroll wheel).
 */
public record AdjustGogglesPayload(String key, float delta) implements CustomPacketPayload {
    public static final Type<AdjustGogglesPayload> TYPE = new Type<>(WatermelonMod.id("adjust_goggles"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustGogglesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AdjustGogglesPayload::key,
            ByteBufCodecs.FLOAT,
            AdjustGogglesPayload::delta,
            AdjustGogglesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
