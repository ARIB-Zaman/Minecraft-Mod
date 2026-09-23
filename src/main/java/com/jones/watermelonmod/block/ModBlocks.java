package com.jones.watermelonmod.block;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.block.custom.WorkBench;
import com.jones.watermelonmod.block.custom.RadiationShriekerBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {
    public static final Block FIRST_BLOCK = registerBlock("first_block",
            properties -> new Block(properties.strength(2f)
                    .sound(SoundType.AMETHYST))
            );
    public static final Block WORKBENCH = registerBlock("workbench",
            properties -> new WorkBench(properties.strength(2.5F).sound(SoundType.AMETHYST))
    );
    public static final Block RADIATION_SHRIEKER = registerBlock("radiation_shrieker",
            properties -> new RadiationShriekerBlock(properties.strength(3.0F, 3.0F).sound(SoundType.SCULK_SHRIEKER))
    );

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function){
        Block toRegister = function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name))));
        registerBlockItem(name, toRegister);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block){
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name)))));
    }

    public static void registerModBlocks(){
        WatermelonMod.LOGGER.info("Registering Mod Block for " + WatermelonMod.MOD_ID);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(FIRST_BLOCK);
            output.accept(WORKBENCH);
            output.accept(RADIATION_SHRIEKER);
        });
    }
}
