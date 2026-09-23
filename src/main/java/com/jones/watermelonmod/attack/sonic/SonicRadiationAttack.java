package com.jones.watermelonmod.attack.sonic;

import com.jones.watermelonmod.attack.SignalBearingAttack;
import com.jones.watermelonmod.signal.SonicSignal;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/** Concrete signal-bearing attack created at the instant Sonic Radiation fires. */
public record SonicRadiationAttack(
        UUID instanceId,
        Identifier typeId,
        UUID sourceEntityId,
        UUID targetEntityId,
        long firedAtGameTime,
        SonicSignal signal
) implements SignalBearingAttack {
}
