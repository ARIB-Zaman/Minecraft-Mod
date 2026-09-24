package com.jones.watermelonmod.datagen;

import com.jones.watermelonmod.block.ModBlocks;
import com.jones.watermelonmod.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.FIRST_BLOCK);
        blockModelGenerators.createTrivialCube(ModBlocks.WORKBENCH);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.FirstItem, ModelTemplates.FLAT_ITEM);
        // Most goggles use authored 3D item models under assets/watermelonmod; only
        // items without one yet fall back to a generated flat icon.
        itemModelGenerators.generateFlatItem(ModItems.HIGH_PASS_GOGGLES, ModelTemplates.FLAT_ITEM);
    }
}
