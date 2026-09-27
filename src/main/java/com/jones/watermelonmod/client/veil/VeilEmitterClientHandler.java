package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.veil.VeilEmitterTracker;
import net.minecraft.client.Minecraft;

/**
 * Applies a Veil Emitter's blur to the player's view whenever they are in
 * range. Runs every client tick, before {@code GpuFftProcessor.tick}, so the
 * FFT sees this frame's degradation state.
 */
public final class VeilEmitterClientHandler {
    private VeilEmitterClientHandler() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null) {
            return;
        }
        VeilEmitterTracker.nearestInRange(client.player.position()).ifPresent(VeilClientState::setDegradation);
    }
}
