package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.Map;

/** Echolocation goggles: the sonar ping keybind sweeps the world and draws a radar HUD while these are worn. */
public final class SonarGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("sonar");
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(PIPELINE_ID, Map.of());

    public SonarGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x2E8B57)));
    }
}
