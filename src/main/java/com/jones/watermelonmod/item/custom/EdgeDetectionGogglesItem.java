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

/** Sobel edge detector whose 3x3 convolution kernel can be rotated in the workbench. */
public final class EdgeDetectionGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("edge_detection");
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(
            PIPELINE_ID,
            Map.of("rotation", new GogglesParameter("rotation", 0.0F, 0.0F, 3.0F))
    );
    private static final int[] BASE_SOBEL_KERNEL = {-1, 0, 1, -2, 0, 2, -1, 0, 1};

    public EdgeDetectionGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x4A8CC7)));
    }

    /** Returns the displayed convolution matrix after 0–3 clockwise quarter-turns. */
    public static int[] kernelForRotation(int quarterTurns) {
        int[] kernel = BASE_SOBEL_KERNEL.clone();
        for (int turn = 0; turn < Math.floorMod(quarterTurns, 4); turn++) {
            int[] rotated = new int[9];
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    rotated[column * 3 + (2 - row)] = kernel[row * 3 + column];
                }
            }
            kernel = rotated;
        }
        return kernel;
    }
}
