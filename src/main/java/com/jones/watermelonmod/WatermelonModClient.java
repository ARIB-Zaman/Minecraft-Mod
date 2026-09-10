package com.jones.watermelonmod;

import com.jones.watermelonmod.client.goggles.GogglesPipelineRegistry;
import com.jones.watermelonmod.client.goggles.GogglesPostProcessingController;
import com.jones.watermelonmod.client.goggles.PostEffectGogglesPipeline;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.client.workbench.WorkbenchScreen;
import com.jones.watermelonmod.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only bridge between equipped goggles and Minecraft's post-effect renderer. */
public final class WatermelonModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.WORKBENCH, WorkbenchScreen::new);
        GogglesPipelineRegistry.register(
                GreyscaleGogglesItem.PIPELINE_ID,
                new PostEffectGogglesPipeline(WatermelonMod.id("greyscale"))
        );
        GogglesPipelineRegistry.register(
                EdgeDetectionGogglesItem.PIPELINE_ID,
                new PostEffectGogglesPipeline(WatermelonMod.id("edge_detection"))
        );
        GogglesPipelineRegistry.register(
                SharpeningGogglesItem.PIPELINE_ID,
                new PostEffectGogglesPipeline(WatermelonMod.id("sharpening"))
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> GogglesPostProcessingController.tick(client));
    }
}
