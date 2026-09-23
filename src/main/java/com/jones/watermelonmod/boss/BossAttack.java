package com.jones.watermelonmod.boss;

import net.minecraft.resources.Identifier;

/**
 * A named capability available to a boss subphase. Execution is intentionally
 * not part of this interface: individual attacks will own that concern.
 */
public interface BossAttack {
    Identifier id();
}
