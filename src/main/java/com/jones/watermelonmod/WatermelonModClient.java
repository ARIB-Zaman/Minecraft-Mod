package com.jones.watermelonmod;

import com.jones.watermelonmod.client.goggles.GogglesPipelineRegistry;
import com.jones.watermelonmod.client.goggles.GogglesPostProcessingController;
import com.jones.watermelonmod.client.goggles.PostEffectGogglesPipeline;
import com.jones.watermelonmod.client.fft.GpuFftProcessor;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.client.workbench.WorkbenchScreen;
import com.jones.watermelonmod.menu.ModMenus;
import com.jones.watermelonmod.client.entity.RadiationWardenRenderer;
import com.jones.watermelonmod.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** Client-only bridge between equipped goggles and Minecraft's post-effect renderer. */
public final class WatermelonModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.RADIATION_WARDEN, RadiationWardenRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_DOME, NoopRenderer::new);
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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GogglesPostProcessingController.tick(client);
            GpuFftProcessor.tick(client);
        });
    }
}
