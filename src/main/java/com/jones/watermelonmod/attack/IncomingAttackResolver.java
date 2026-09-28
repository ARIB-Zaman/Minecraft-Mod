package com.jones.watermelonmod.attack;

import com.jones.watermelonmod.echofunnel.EchoFunnelCaptureService;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import com.jones.watermelonmod.entity.SilenceDomeEntity;
import com.jones.watermelonmod.network.VeilBlindPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * The single boundary between a readable attack payload and damage delivery.
 */
public final class IncomingAttackResolver {
    private IncomingAttackResolver() {
    }

    public static boolean resolve(ServerLevel level, LivingEntity target, IncomingAttack attack, DamageSource damageSource, float damage) {
        if (target instanceof Player player && SilenceDomeEntity.protects(level, player)) {
            return false;
        }
        if (target instanceof Player player && attack instanceof SignalBearingAttack signalAttack
                && EchoFunnelCaptureService.tryCapture(player, signalAttack)) {
            return false;
        }
        if (target instanceof ServerPlayer serverPlayer && attack instanceof SignalBearingAttack signalAttack) {
            blindWithVeil(level, serverPlayer, signalAttack);
        }
        return target.hurtServer(level, damageSource, damage);
    }

    /**
     * A landed signal-bearing hit also briefly blurs the target's view; the
     * recovery window shrinks as the attack's source weakens, so the fight
     * gets harder the longer it goes on without changing its damage at all.
     */
    private static void blindWithVeil(ServerLevel level, ServerPlayer target, SignalBearingAttack attack) {
        Entity source = level.getEntity(attack.sourceEntityId());
        if (!(source instanceof RadiationWardenEntity warden)) {
            return;
        }
        double healthFraction = warden.getHealth() / warden.getMaxHealth();
        ServerPlayNetworking.send(target, new VeilBlindPayload(windowSecondsFor(healthFraction)));
    }

    private static int windowSecondsFor(double healthFraction) {
        if (healthFraction > 0.75) {
            return 6;
        }
        if (healthFraction > 0.50) {
            return 5;
        }
        if (healthFraction > 0.25) {
            return 4;
        }
        return 3;
    }
}
