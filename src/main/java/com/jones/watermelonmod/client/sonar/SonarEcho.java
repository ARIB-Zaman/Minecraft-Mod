package com.jones.watermelonmod.client.sonar;

import net.minecraft.world.phys.Vec3;

/**
 * One echo returned by a sonar ping: a block surface or a nearby mob.
 * Conceptually this is a discrete, time-shifted copy of the outgoing pulse — {@code revealTick}
 * is the client tick (see {@link SonarState#clientTick()}) at which the real round-trip travel
 * time {@code τ = 2d/v} has elapsed and the echo should actually appear in the world.
 */
public record SonarEcho(Vec3 worldPos, int argbColor, int revealTick, boolean isMob, float dopplerRatio) {
}
