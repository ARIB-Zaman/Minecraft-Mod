package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.LinkedHashMap;
import java.util.Map;

/** Frequency-domain goggles: a 1024x512 RGB FFT with an adjustable high-pass cutoff. */
public final class HighPassGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("high_pass_filter");
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(PIPELINE_ID, parameters());

    public HighPassGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x4F8FBF)));
    }

    private static Map<String, GogglesParameter> parameters() {
        Map<String, GogglesParameter> parameters = new LinkedHashMap<>();
        parameters.put("cutoff", new GogglesParameter("cutoff", 0.10F, 0.01F, 1.0F));
        // Zero disables the overlay while retaining a simple slider-based UI.
        parameters.put("spectrum_opacity", new GogglesParameter("spectrum_opacity", 0.75F, 0.0F, 1.0F));
        return parameters;
    }
}
