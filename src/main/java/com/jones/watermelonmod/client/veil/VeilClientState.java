package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.veil.VeilKernel;

import java.util.Optional;

/**
 * The blur the Veil currently applies to this player's view. Set by the
 * {@code /veil} test command, or by {@code VeilEmitterClientHandler} whenever
 * the player is within range of a Veil Emitter block.
 */
public final class VeilClientState {
    private static VeilKernel degradation;

    private VeilClientState() {
    }

    public static Optional<VeilKernel> degradation() {
        return Optional.ofNullable(degradation);
    }

    public static void setDegradation(VeilKernel kernel) {
        degradation = kernel;
    }

    public static void clear() {
        degradation = null;
    }
}
