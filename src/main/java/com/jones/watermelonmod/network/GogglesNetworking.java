package com.jones.watermelonmod.network;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Validates and applies live goggles-tuning requests (sneak + scroll) on the logical server. */
public final class GogglesNetworking {
    private GogglesNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(AdjustGogglesPayload.TYPE, AdjustGogglesPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AdjustGogglesPayload.TYPE, (payload, context) ->
                adjust(context.player(), payload));
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
