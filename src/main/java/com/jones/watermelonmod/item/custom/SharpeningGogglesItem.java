package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

/** RGB sharpening filter using an editable high-pass convolution kernel. */
public final class SharpeningGogglesItem extends ConvolutionGogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("sharpening");
    private static final int[] DEFAULT_SHARPEN_KERNEL = {0, -1, 0, -1, 5, -1, 0, -1, 0};
    public static final GogglesPipeline PIPELINE = convolutionPipeline(PIPELINE_ID, DEFAULT_SHARPEN_KERNEL);

    public SharpeningGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE, DEFAULT_SHARPEN_KERNEL,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0xD89835)));
    }
}
