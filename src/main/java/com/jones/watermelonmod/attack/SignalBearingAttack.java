package com.jones.watermelonmod.attack;

import com.jones.watermelonmod.signal.SonicSignal;

/** Marks attacks whose payload can be captured by the future Mixer. */
public interface SignalBearingAttack extends IncomingAttack {
    SonicSignal signal();
}
