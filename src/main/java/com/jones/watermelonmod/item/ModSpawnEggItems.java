package com.jones.watermelonmod.item;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.entity.ModEntities;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/** Test-only access to the boss until its redstone spawning mechanic exists. */
public final class ModSpawnEggItems {
    public static final Item RADIATION_WARDEN_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            WatermelonMod.id("radiation_warden_spawn_egg"),
            new SpawnEggItem(new Item.Properties()
                    .spawnEgg(ModEntities.RADIATION_WARDEN)
                    .setId(ResourceKey.create(Registries.ITEM, WatermelonMod.id("radiation_warden_spawn_egg"))))
    );

    private ModSpawnEggItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> output.accept(RADIATION_WARDEN_SPAWN_EGG));
    }
}
