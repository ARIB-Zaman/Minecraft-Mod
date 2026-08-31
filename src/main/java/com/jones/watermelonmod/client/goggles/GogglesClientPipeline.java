package com.jones.watermelonmod.client.goggles;

import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/** Client-side implementation of a DSP pipeline. */
public interface GogglesClientPipeline {
    Optional<Identifier> postEffect(GogglesSettings settings);
}
