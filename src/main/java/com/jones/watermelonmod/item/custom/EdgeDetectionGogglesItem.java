package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;


/** Edge detector with a per-item, editable 3x3 convolution kernel. */
public final class EdgeDetectionGogglesItem extends ConvolutionGogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("edge_detection");
    private static final int[] BASE_SOBEL_KERNEL = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
    public static final GogglesPipeline PIPELINE = convolutionPipeline(PIPELINE_ID, BASE_SOBEL_KERNEL);

    public EdgeDetectionGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE, BASE_SOBEL_KERNEL,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x4A8CC7)));
    }
}
