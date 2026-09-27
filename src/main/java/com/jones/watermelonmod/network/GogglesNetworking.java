package com.jones.watermelonmod.network;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Validates and applies live goggles-tuning requests (sneak + scroll) on the logical server. */
public final class GogglesNetworking {
    private GogglesNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(AdjustGogglesPayload.TYPE, AdjustGogglesPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AdjustGogglesPayload.TYPE, (payload, context) ->
                adjust(context.player(), payload));
        PayloadTypeRegistry.clientboundPlay().register(VeilBlindPayload.TYPE, VeilBlindPayload.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(VeilRecomposedPayload.TYPE, VeilRecomposedPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(VeilRecomposedPayload.TYPE, (payload, context) ->
                recompose(context.player(), payload.convergence()));

        PayloadTypeRegistry.clientboundPlay().register(VeilConvergencePayload.TYPE, VeilConvergencePayload.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(VeilConvergenceFailedPayload.TYPE, VeilConvergenceFailedPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(VeilConvergenceFailedPayload.TYPE, (payload, context) ->
                convergenceFailed(context.player()));
    }

    /** A well-timed recompose briefly stuns the nearest Radiation Warden — longer after surviving a Convergence. */
    private static final int RECOMPOSE_STAGGER_TICKS = 30;
    private static final int CONVERGENCE_STAGGER_TICKS = 60;
    /** Extra damage taken for failing to recompose a Convergence in time. */
    private static final float CONVERGENCE_FAILURE_DAMAGE = 12.0F;

    private static void recompose(ServerPlayer player, boolean convergence) {
        ServerLevel level = (ServerLevel) player.level();
        int ticks = convergence ? CONVERGENCE_STAGGER_TICKS : RECOMPOSE_STAGGER_TICKS;
        level.getEntitiesOfClass(RadiationWardenEntity.class, player.getBoundingBox().inflate(32.0))
                .stream().findFirst()
                .ifPresent(warden -> warden.freezeByBreeze(ticks));
    }

    private static void convergenceFailed(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        player.hurtServer(level, level.damageSources().generic(), CONVERGENCE_FAILURE_DAMAGE);
    }

    private static void adjust(ServerPlayer player, AdjustGogglesPayload payload) {
        GogglesEquipment.equippedGoggles(player).ifPresent(stack -> {
            if (!(stack.getItem() instanceof GogglesItem goggles)) {
                return;
            }
            GogglesParameter parameter = goggles.pipeline().parameters().get(payload.key());
            if (parameter == null) {
                return;
            }
            float current = GogglesSettingsService.get(stack).value(payload.key(), parameter.defaultValue());
            GogglesSettingsService.setParameter(stack, payload.key(), current + payload.delta());
        });
    }
}
