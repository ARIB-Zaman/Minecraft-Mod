package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.goggles.GogglesPipeline;
import com.jones.watermelonmod.item.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Base item for goggles that occupy the helmet slot.
 */
public class GogglesItem extends Item {
    private final GogglesPipeline pipeline;

    public GogglesItem(ArmorMaterial armorMaterial, GogglesPipeline pipeline, Properties properties) {
        super(properties
                // Retain leather helmet attributes, durability, and equipping
                // behaviour without an armor asset. Minecraft then renders the
                // item's own `display.head` model instead of leather armor.
                .durability(ArmorType.HELMET.getDurability(armorMaterial.durability()))
                .attributes(armorMaterial.createAttributes(ArmorType.HELMET))
                .enchantable(armorMaterial.enchantmentValue())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(armorMaterial.equipSound())
                        .build())
                .repairable(armorMaterial.repairIngredient())
                .component(ModDataComponents.GOGGLES_SETTINGS, pipeline.defaultSettings()));
        this.pipeline = pipeline;
    }

    public GogglesPipeline pipeline() {
        return pipeline;
    }
}
