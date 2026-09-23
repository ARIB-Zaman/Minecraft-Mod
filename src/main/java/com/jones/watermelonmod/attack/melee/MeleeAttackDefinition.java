package com.jones.watermelonmod.attack.melee;

import com.jones.watermelonmod.boss.BossAttack;
import net.minecraft.resources.Identifier;

/** Tuning for the Warden-style close-range strike, kept separate from its goal logic. */
public record MeleeAttackDefinition(Identifier id, int cooldownTicks, double engagementRange) implements BossAttack {
    public MeleeAttackDefinition {
        if (cooldownTicks <= 0 || engagementRange <= 0.0) {
            throw new IllegalArgumentException("Invalid melee attack definition");
        }
    }
}
