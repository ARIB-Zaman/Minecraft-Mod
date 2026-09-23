package com.jones.watermelonmod.item;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.item.custom.FrequencyFilterGogglesItem;
import com.jones.watermelonmod.item.custom.BandPassGogglesItem;
import com.jones.watermelonmod.item.custom.EchoFunnelItem;
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
    public static final Item GREYSCALE_GOGGLES = registerItem("greyscale_goggles", GreyscaleGogglesItem::new);
    public static final Item EDGE_DETECTION_GOGGLES = registerItem("edge_detection_goggles", EdgeDetectionGogglesItem::new);
    public static final Item SHARPENING_GOGGLES = registerItem("sharpening_goggles", SharpeningGogglesItem::new);
    public static final Item FREQUENCY_FILTER_GOGGLES = registerItem("frequency_filter_goggles", FrequencyFilterGogglesItem::new);
    public static final Item BAND_PASS_GOGGLES = registerItem("band_pass_goggles", BandPassGogglesItem::new);
    public static final Item ECHO_FUNNEL = registerItem("echo_funnel", EchoFunnelItem::new);

    private static Item registerItem(String name, Function<Item.Properties, Item> function){
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(WatermelonMod.MOD_ID, name)))));
    }

    public static void registerModItems(){
        WatermelonMod.LOGGER.info("Registering Mod Items for " + WatermelonMod.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(FirstItem);
            output.accept(GREYSCALE_GOGGLES);
            output.accept(EDGE_DETECTION_GOGGLES);
            output.accept(SHARPENING_GOGGLES);
            output.accept(FREQUENCY_FILTER_GOGGLES);
            output.accept(BAND_PASS_GOGGLES);
            output.accept(ECHO_FUNNEL);
        });
    }
}
