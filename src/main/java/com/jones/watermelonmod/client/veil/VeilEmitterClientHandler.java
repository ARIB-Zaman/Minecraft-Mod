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
        // A running trial owns the blur exclusively; an ambient emitter must not
        // fight it for the same shared degradation state tick by tick.
        if (client.player == null || VeilTrialState.isActive()) {
            return;
        }
        var nearby = VeilEmitterTracker.nearestInRange(client.player.position());
        if (nearby.isPresent()) {
            VeilClientState.setDegradation(nearby.get(), VeilClientState.Source.EMITTER);
        } else {
            // Leaving the radius must turn the blur off again — but only if an
            // emitter was the one that turned it on, not a manual /veil command.
            VeilClientState.clearIfSource(VeilClientState.Source.EMITTER);
        }
    }
}
