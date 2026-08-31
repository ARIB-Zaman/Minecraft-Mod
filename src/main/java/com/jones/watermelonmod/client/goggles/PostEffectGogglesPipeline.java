package com.jones.watermelonmod.client.goggles;

import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/** Reusable adapter for JSON/GLSL post-effect chains supplied through Minecraft resources. */
public record PostEffectGogglesPipeline(Identifier postEffectId) implements GogglesClientPipeline {
    @Override
    public Optional<Identifier> postEffect(GogglesSettings settings) {
        return settings.value("enabled", 1.0F) >= 0.5F ? Optional.of(postEffectId) : Optional.empty();
    }
}
