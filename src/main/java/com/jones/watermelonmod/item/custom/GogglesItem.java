package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.goggles.GogglesPipeline;
import com.jones.watermelonmod.item.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Base item for goggles that occupy the helmet slot.
 */
public class GogglesItem extends Item {
    private final GogglesPipeline pipeline;

    public GogglesItem(ArmorMaterial armorMaterial, GogglesPipeline pipeline, Properties properties) {
        super(properties
                .humanoidArmor(armorMaterial, ArmorType.HELMET)
                .component(ModDataComponents.GOGGLES_SETTINGS, pipeline.defaultSettings()));
        this.pipeline = pipeline;
    }

    public GogglesPipeline pipeline() {
        return pipeline;
    }
}
