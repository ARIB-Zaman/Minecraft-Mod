package com.jones.watermelonmod.client.sonar;

import net.minecraft.world.phys.Vec3;

/**
 * One echo returned by a sonar ping: a block surface or a nearby mob.
 * {@code revealTick} is the client tick (see {@link SonarState#clientTick()}) at which this echo's
 * real round-trip travel time has elapsed and it should start being drawn.
 */
public record SonarBlip(Vec3 worldPos, int argbColor, int revealTick, boolean isMob, float dopplerRatio) {
}
