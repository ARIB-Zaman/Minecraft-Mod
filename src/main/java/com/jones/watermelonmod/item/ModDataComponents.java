package com.jones.watermelonmod.item;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Data stored on item stacks rather than in client render state.
 */
public final class ModDataComponents {
    public static final DataComponentType<GogglesSettings> GOGGLES_SETTINGS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            WatermelonMod.id("goggles_settings"),
            DataComponentType.<GogglesSettings>builder()
                    .persistent(GogglesSettings.CODEC)
                    .networkSynchronized(GogglesSettings.STREAM_CODEC)
                    .build()
    );

    private ModDataComponents() {
    }

    public static void initialize() {
        // Forces registration before items use this component in their default properties.
    }
}
