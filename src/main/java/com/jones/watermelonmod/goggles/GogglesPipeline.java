package com.jones.watermelonmod.goggles;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common, client-independent description of a goggles DSP pipeline.
 */
public record GogglesPipeline(Identifier id, Map<String, GogglesParameter> parameters) {
    public GogglesPipeline {
        parameters = Map.copyOf(parameters);
    }

    public GogglesSettings defaultSettings() {
        Map<String, Float> defaults = new LinkedHashMap<>();
        parameters.forEach((key, parameter) -> defaults.put(key, parameter.defaultValue()));
        return new GogglesSettings(defaults);
    }

    public GogglesSettings normalize(GogglesSettings settings) {
        Map<String, Float> normalized = new LinkedHashMap<>(settings.parameters());
        parameters.forEach((key, parameter) -> normalized.put(key, parameter.clamp(settings.value(key, parameter.defaultValue()))));
        return new GogglesSettings(normalized);
    }
}
