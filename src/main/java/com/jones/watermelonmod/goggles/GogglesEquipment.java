package com.jones.watermelonmod.goggles;

import com.jones.watermelonmod.item.custom.GogglesItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Shared equipment lookup for gameplay systems, menus, and client rendering.
 */
public final class GogglesEquipment {
    private GogglesEquipment() {
    }

    public static Optional<ItemStack> equippedGoggles(LivingEntity entity) {
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        return headStack.getItem() instanceof GogglesItem ? Optional.of(headStack) : Optional.empty();
    }
}
