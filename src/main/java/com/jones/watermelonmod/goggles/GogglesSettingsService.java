package com.jones.watermelonmod.goggles;

import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.item.custom.GogglesItem;
import net.minecraft.world.item.ItemStack;

/**
 * Item-side API for workbenches, menus, and networking to update DSP settings.
 */
public final class GogglesSettingsService {
    private GogglesSettingsService() {
    }

    public static GogglesSettings get(ItemStack stack) {
        if (!(stack.getItem() instanceof GogglesItem goggles)) {
            return new GogglesSettings(java.util.Map.of());
        }

        return goggles.pipeline().normalize(stack.getOrDefault(ModDataComponents.GOGGLES_SETTINGS, goggles.pipeline().defaultSettings()));
    }

    public static void setParameter(ItemStack stack, String key, float value) {
        if (!(stack.getItem() instanceof GogglesItem goggles)) {
            throw new IllegalArgumentException("DSP settings can only be assigned to goggles");
        }

        GogglesPipeline pipeline = goggles.pipeline();
        GogglesParameter parameter = pipeline.parameters().get(key);
        if (parameter == null) {
            throw new IllegalArgumentException("Unknown goggles DSP parameter: " + key);
        }

        stack.set(ModDataComponents.GOGGLES_SETTINGS, pipeline.normalize(get(stack).withValue(key, parameter.clamp(value))));
    }
}
