package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.Map;

/**
 * Goggles that select the grayscale post-processing pipeline.
 */
public final class GreyscaleGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("greyscale");
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(
            PIPELINE_ID,
            Map.of("intensity", new GogglesParameter("intensity", 1.0F, 0.0F, 1.0F))
    );

    public GreyscaleGogglesItem(Item.Properties properties) {
        super(
                ArmorMaterials.LEATHER,
                PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x7F7F7F))
        );
    }
}
