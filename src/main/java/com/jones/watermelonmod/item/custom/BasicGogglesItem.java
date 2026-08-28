package com.jones.watermelonmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

/**
 * A basic, leather-tier goggles item with a grayscale texture.
 */
public class BasicGogglesItem extends GogglesItem {
    public BasicGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x7F7F7F)));
    }
}
