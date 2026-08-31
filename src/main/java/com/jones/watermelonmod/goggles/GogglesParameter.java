package com.jones.watermelonmod.goggles;

/**
 * Describes one runtime-configurable value exposed by a goggles DSP pipeline.
 */
public record GogglesParameter(String key, float defaultValue, float minimum, float maximum) {
    public GogglesParameter {
        if (minimum > maximum) {
            throw new IllegalArgumentException("The minimum value cannot exceed the maximum value");
        }
    }

    public float clamp(float value) {
        return Math.clamp(value, minimum, maximum);
    }
}
