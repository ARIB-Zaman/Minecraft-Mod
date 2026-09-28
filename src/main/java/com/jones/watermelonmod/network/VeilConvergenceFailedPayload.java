package com.jones.watermelonmod.network;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client tells the server a Convergence trial ran out without being recomposed, so the follow-up penalty applies. */
public record VeilConvergenceFailedPayload(int angularDistanceDegrees) implements CustomPacketPayload {
    public static final Type<VeilConvergenceFailedPayload> TYPE = new Type<>(WatermelonMod.id("veil_convergence_failed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeilConvergenceFailedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VeilConvergenceFailedPayload::angularDistanceDegrees,
            VeilConvergenceFailedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
