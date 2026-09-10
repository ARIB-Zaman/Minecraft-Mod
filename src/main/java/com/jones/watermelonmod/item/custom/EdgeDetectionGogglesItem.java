package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.Map;
import java.util.LinkedHashMap;

/** Edge detector with a per-item, editable 3x3 convolution kernel. */
public final class EdgeDetectionGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("edge_detection");
    private static final int[] BASE_SOBEL_KERNEL = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(PIPELINE_ID, kernelParameters());

    public EdgeDetectionGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x4A8CC7)));
    }

    public static String coefficientKey(int index) {
        return "kernel_" + index;
    }

    public static int[] kernel(GogglesSettings settings) {
        int[] kernel = new int[9];
        for (int index = 0; index < kernel.length; index++) {
            kernel[index] = Math.round(settings.value(coefficientKey(index), BASE_SOBEL_KERNEL[index]));
        }
        return kernel;
    }

    private static Map<String, GogglesParameter> kernelParameters() {
        Map<String, GogglesParameter> parameters = new LinkedHashMap<>();
        for (int index = 0; index < BASE_SOBEL_KERNEL.length; index++) {
            parameters.put(coefficientKey(index), new GogglesParameter(coefficientKey(index), BASE_SOBEL_KERNEL[index], -4.0F, 4.0F));
        }
        return parameters;
    }
}
