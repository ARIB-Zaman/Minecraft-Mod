package com.jones.watermelonmod.client.goggles;

import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/** Reusable adapter for JSON/GLSL post-effect chains supplied through Minecraft resources. */
public record PostEffectGogglesPipeline(Identifier postEffectId) implements GogglesClientPipeline {
    @Override
    public Optional<Identifier> postEffect(GogglesSettings settings) {
        // An intensity of zero is an identity pass, not an on/off threshold.
        // The post-pass mixin uploads the current parameter every rendered frame.
        return Optional.of(postEffectId);
    }
}
