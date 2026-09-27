package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;

import java.util.List;

/**
 * Which of the equipped Veil Goggles' parameters sneak + scroll currently
 * adjusts. Purely client-side display/selection state; the actual value
 * lives on the item stack, mutated through {@code AdjustGogglesPayload}.
 */
public final class VeilTuningState {
    private static int selectedIndex;

    private VeilTuningState() {
    }

    public static GogglesParameter selected(GogglesPipeline pipeline) {
        List<GogglesParameter> parameters = List.copyOf(pipeline.parameters().values());
        selectedIndex = Math.floorMod(selectedIndex, parameters.size());
        return parameters.get(selectedIndex);
    }

    public static void cycle(GogglesPipeline pipeline) {
        int count = pipeline.parameters().size();
        selectedIndex = Math.floorMod(selectedIndex + 1, Math.max(1, count));
    }

    /** Explicitly points selection at one parameter, e.g. so a trial leaves scrolling on the same setting once it ends. */
    public static void select(GogglesPipeline pipeline, String key) {
        List<String> keys = List.copyOf(pipeline.parameters().keySet());
        int index = keys.indexOf(key);
        if (index >= 0) {
            selectedIndex = index;
        }
    }
}
