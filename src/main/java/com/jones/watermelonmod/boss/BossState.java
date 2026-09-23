package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

/** Persistent runtime selection; attack cooldowns and transitions come later. */
public record BossState(Identifier profileId, Identifier phaseId, Identifier subphaseId) {
}
