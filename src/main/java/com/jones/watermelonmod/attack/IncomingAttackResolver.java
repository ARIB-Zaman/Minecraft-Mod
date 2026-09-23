package com.jones.watermelonmod.attack;

import com.jones.watermelonmod.echofunnel.EchoFunnelCaptureService;
import com.jones.watermelonmod.entity.SilenceDomeEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
        return target.hurtServer(level, damageSource, damage);
    }
}
