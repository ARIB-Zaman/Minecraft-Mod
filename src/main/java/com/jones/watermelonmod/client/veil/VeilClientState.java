package com.jones.watermelonmod.client.veil;

import java.util.Optional;

/**
 * The blur the Veil currently applies to this player's view. Only the
 * {@code /veil} test command sets it for now; Veil zones will later feed the
 * same state.
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
