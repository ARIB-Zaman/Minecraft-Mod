package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.veil.VeilKernel;

import java.util.Optional;

/**
 * The blur the Veil currently applies to this player's view, plus which
 * system currently owns it (an ambient Veil Emitter block, a manual
 * {@code /veil} command, or a timed trial). Tracking the owner lets one
 * source clear its own blur without clobbering a different source's —
 * e.g. leaving an emitter's radius must not cancel a manually-set test blur.
 */
public final class VeilClientState {
    public enum Source {
        MANUAL,
        EMITTER,
        TRIAL
    }

    private static VeilKernel degradation;
    private static Source source;

    private VeilClientState() {
    }

    public static Optional<VeilKernel> degradation() {
        return Optional.ofNullable(degradation);
    }

    public static void setDegradation(VeilKernel kernel, Source source) {
        VeilClientState.degradation = kernel;
        VeilClientState.source = source;
    }

    public static void clear() {
        degradation = null;
        source = null;
    }

    /** Clears only if the current blur is still owned by {@code expectedSource}. */
    public static void clearIfSource(Source expectedSource) {
        if (source == expectedSource) {
            clear();
        }
    }
}
