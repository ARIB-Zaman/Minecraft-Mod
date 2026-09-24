package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client request to redeem an offer already selected in the Echo Funnel UI. */
public record RedeemEchoFunnelRewardPayload(int offerIndex) implements CustomPacketPayload {
    public static final Type<RedeemEchoFunnelRewardPayload> TYPE = new Type<>(WatermelonMod.id("redeem_echo_funnel_reward"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RedeemEchoFunnelRewardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RedeemEchoFunnelRewardPayload::offerIndex, RedeemEchoFunnelRewardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
