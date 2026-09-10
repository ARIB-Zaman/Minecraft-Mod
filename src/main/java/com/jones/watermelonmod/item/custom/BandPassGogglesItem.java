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

/** FFT goggles with a hard annular frequency-domain pass band. */
public final class BandPassGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("band_pass");
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(PIPELINE_ID, parameters());

    public BandPassGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0xB58A32)));
    }

    private static Map<String, GogglesParameter> parameters() {
        Map<String, GogglesParameter> parameters = new LinkedHashMap<>();
        // Radius is in cycles-per-pixel: the corner of the centred spectrum is about 0.707.
        parameters.put("low_cutoff", new GogglesParameter("low_cutoff", 0.05F, 0.0F, 0.71F));
        parameters.put("high_cutoff", new GogglesParameter("high_cutoff", 0.25F, 0.0F, 0.71F));
        return parameters;
    }
}
