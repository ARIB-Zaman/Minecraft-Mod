package com.jones.watermelonmod;

import com.jones.watermelonmod.client.goggles.GogglesPipelineRegistry;
import com.jones.watermelonmod.client.goggles.GogglesPostProcessingController;
import com.jones.watermelonmod.client.goggles.PostEffectGogglesPipeline;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Client-only bridge between equipped goggles and Minecraft's post-effect renderer. */
public final class WatermelonModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GogglesPipelineRegistry.register(
                GreyscaleGogglesItem.PIPELINE_ID,
                new PostEffectGogglesPipeline(WatermelonMod.id("greyscale"))
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> GogglesPostProcessingController.tick(client));
    }
}
