package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.Optional;

/**
 * Chooses an attack identifier from the currently active subphase. A later
 * implementation may use weights, cooldowns, target distance, or history.
 */
@FunctionalInterface
public interface BossAttackSelector {
    Optional<Identifier> select(BossSubphase subphase, RandomSource random);
}
