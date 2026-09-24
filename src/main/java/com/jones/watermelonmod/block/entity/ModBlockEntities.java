package com.jones.watermelonmod.block.entity;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Central registration point for block-backed inventories. */
public final class ModBlockEntities {
    public static final BlockEntityType<WorkbenchBlockEntity> WORKBENCH = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            WatermelonMod.id("workbench"),
            new BlockEntityType<>(WorkbenchBlockEntity::new, java.util.Set.of(ModBlocks.WORKBENCH))
    );
    public static final BlockEntityType<RadiationShriekerBlockEntity> RADIATION_SHRIEKER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            WatermelonMod.id("radiation_shrieker"),
            new BlockEntityType<>(RadiationShriekerBlockEntity::new, java.util.Set.of(ModBlocks.RADIATION_SHRIEKER))
    );

    private ModBlockEntities() { }

    public static void initialize() { }
}
