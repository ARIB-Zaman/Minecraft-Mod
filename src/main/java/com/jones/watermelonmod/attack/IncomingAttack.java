package com.jones.watermelonmod.attack;

import net.minecraft.resources.Identifier;

import java.util.UUID;

/** A server-side attack payload that a future defensive system may inspect. */
public interface IncomingAttack {
    UUID instanceId();

    Identifier typeId();

    UUID sourceEntityId();

    UUID targetEntityId();
}
