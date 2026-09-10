package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import com.jones.watermelonmod.goggles.GogglesSettings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared item model for goggles driven by a user-editable 3x3 convolution matrix. */
public abstract class ConvolutionGogglesItem extends GogglesItem {
    private final int[] defaultKernel;

    protected ConvolutionGogglesItem(ArmorMaterial material, GogglesPipeline pipeline, int[] defaultKernel, Item.Properties properties) {
        super(material, pipeline, properties);
        if (defaultKernel.length != 9) throw new IllegalArgumentException("A convolution kernel must be 3x3");
        this.defaultKernel = defaultKernel.clone();
    }

    public static String coefficientKey(int index) {
        return "kernel_" + index;
    }

    protected static GogglesPipeline convolutionPipeline(net.minecraft.resources.Identifier id, int[] defaultKernel) {
        Map<String, GogglesParameter> parameters = new LinkedHashMap<>();
        for (int index = 0; index < 9; index++) {
            parameters.put(coefficientKey(index), new GogglesParameter(coefficientKey(index), defaultKernel[index], -10.0F, 10.0F));
        }
        return new GogglesPipeline(id, parameters);
    }

    public int[] kernel(GogglesSettings settings) {
        int[] kernel = new int[9];
        for (int index = 0; index < kernel.length; index++) {
            kernel[index] = Math.round(settings.value(coefficientKey(index), defaultKernel[index]));
        }
        return kernel;
    }
}
