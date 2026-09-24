package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client request to immediately extract one bin from one captured signal. */
public record BankEchoFunnelSignalPayload(int signalSlot, int bin) implements CustomPacketPayload {
    public static final Type<BankEchoFunnelSignalPayload> TYPE = new Type<>(WatermelonMod.id("bank_echo_funnel_signal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BankEchoFunnelSignalPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BankEchoFunnelSignalPayload::signalSlot,
            ByteBufCodecs.VAR_INT,
            BankEchoFunnelSignalPayload::bin,
            BankEchoFunnelSignalPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
