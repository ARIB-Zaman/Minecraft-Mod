package com.jones.watermelonmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Base item for goggles that occupy the helmet slot.
 */
public class GogglesItem extends Item {
    public GogglesItem(ArmorMaterial armorMaterial, Properties properties) {
        super(properties.humanoidArmor(armorMaterial, ArmorType.HELMET));
    }
}
