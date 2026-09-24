package com.jones.watermelonmod.attack.sonic;

import com.jones.watermelonmod.boss.BossAttack;
import com.jones.watermelonmod.signal.SonicSignalTemplate;
import net.minecraft.resources.Identifier;

/** All initial Sonic Radiation tuning belongs here, outside AI control flow. */
public record SonicRadiationAttackDefinition(
        Identifier id,
        int chargeTicks,
        int cooldownTicks,
        double horizontalRange,
        double verticalRange,
        float damage,
        double horizontalKnockback,
        double verticalKnockback,
        SonicSignalTemplate signalTemplate
) implements BossAttack {
    public SonicRadiationAttackDefinition {
        if (chargeTicks <= 0 || cooldownTicks < 0 || horizontalRange <= 0.0 || verticalRange <= 0.0 || damage < 0.0F) {
            throw new IllegalArgumentException("Invalid Sonic Radiation attack definition");
        }
    }
}
