package com.jones.watermelonmod.attack;

import com.jones.watermelonmod.signal.SonicSignal;

import java.util.UUID;

/** Marks attacks whose payload can be captured by the future Mixer. */
public interface SignalBearingAttack extends IncomingAttack {
    SonicSignal signal();

    /** The entity that fired this attack, so a landed hit can react to its current state (e.g. health). */
    UUID sourceEntityId();
}
