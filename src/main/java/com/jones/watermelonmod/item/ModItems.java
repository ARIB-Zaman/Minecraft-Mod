package com.jones.watermelonmod.item;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.item.custom.BasicGogglesItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    //names are all in lowercase, no spaces
    public static final Item FirstItem = registerItem("firstitem", Item::new);
    public static final Item BASIC_GOGGLES = registerItem("basic_goggles", BasicGogglesItem::new);

    private static Item registerItem(String name, Function<Item.Properties, Item> function){
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name)))));
    }

    public static void registerModItems(){
        WatermelonMod.LOGGER.info("Registering Mod Items for " + WatermelonMod.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(FirstItem);
            output.accept(BASIC_GOGGLES);
        });
    }
}
