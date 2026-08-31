package com.jones.watermelonmod.client.goggles;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Registry for client DSP implementations. */
public final class GogglesPipelineRegistry {
    private static final Map<Identifier, GogglesClientPipeline> PIPELINES = new HashMap<>();

    private GogglesPipelineRegistry() {
    }

    public static void register(Identifier id, GogglesClientPipeline pipeline) {
        if (PIPELINES.putIfAbsent(id, pipeline) != null) {
            throw new IllegalStateException("A goggles pipeline is already registered for " + id);
        }
    }

    public static Optional<GogglesClientPipeline> get(Identifier id) {
        return Optional.ofNullable(PIPELINES.get(id));
    }
}
