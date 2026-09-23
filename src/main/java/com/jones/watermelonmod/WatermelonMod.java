package com.jones.watermelonmod;

import com.jones.watermelonmod.block.ModBlocks;
import com.jones.watermelonmod.block.entity.ModBlockEntities;
import com.jones.watermelonmod.menu.ModMenus;
import com.jones.watermelonmod.item.ModItems;
import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.item.ModSpawnEggItems;
import com.jones.watermelonmod.entity.ModEntities;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatermelonMod implements ModInitializer {
	public static final String MOD_ID = "watermelonmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModDataComponents.initialize();
		ModEntities.initialize();
		FabricDefaultAttributeRegistry.register(ModEntities.RADIATION_WARDEN, RadiationWardenEntity.createAttributes());
		ModSpawnEggItems.initialize();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.initialize();
		ModMenus.initialize();
		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
