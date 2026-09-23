package com.jones.watermelonmod.attack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * The single boundary between a readable attack payload and damage delivery.
 * Mixer interception will be added here later without coupling it to a boss.
 */
public final class IncomingAttackResolver {
    private IncomingAttackResolver() {
    }

    public static boolean resolve(ServerLevel level, LivingEntity target, IncomingAttack attack, DamageSource damageSource, float damage) {
        return target.hurtServer(level, damageSource, damage);
    }
}
