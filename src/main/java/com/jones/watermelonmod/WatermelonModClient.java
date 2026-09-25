package com.jones.watermelonmod;

import com.jones.watermelonmod.client.entity.RadiationWardenRenderer;
import com.jones.watermelonmod.client.fft.GpuFftProcessor;
import com.jones.watermelonmod.client.goggles.GogglesPipelineRegistry;
import com.jones.watermelonmod.client.goggles.GogglesPostProcessingController;
import com.jones.watermelonmod.client.goggles.PostEffectGogglesPipeline;
import com.jones.watermelonmod.client.sonar.SonarController;
import com.jones.watermelonmod.client.sonar.SonarHud;
import com.jones.watermelonmod.client.sonar.SonarState;
import com.jones.watermelonmod.client.veil.VeilCommands;
import com.jones.watermelonmod.client.workbench.WorkbenchScreen;
import com.jones.watermelonmod.entity.ModEntities;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.menu.ModMenus;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** Client-only bridge between equipped goggles and Minecraft's post-effect renderer. */
public final class WatermelonModClient implements ClientModInitializer {
    private static final KeyMapping SONAR_PING_KEY = new KeyMapping(
            "key.watermelonmod.sonar_ping", InputConstants.KEY_G, KeyMapping.Category.GAMEPLAY
    );

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.RADIATION_WARDEN, RadiationWardenRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_DOME, NoopRenderer::new);
        EntityRendererRegistry.register(ModEntities.FREEZE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.DAMAGE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        MenuScreens.register(ModMenus.WORKBENCH, WorkbenchScreen::new);
        VeilCommands.register();
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
        KeyMappingHelper.registerKeyMapping(SONAR_PING_KEY);
        HudElementRegistry.addLast(WatermelonMod.id("sonar_hud"), new SonarHud());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GogglesPostProcessingController.tick(client);
            GpuFftProcessor.tick(client);
            SonarState.advanceTick();
            if (SONAR_PING_KEY.consumeClick()) {
                SonarController.ping(client);
            }
            SonarController.tick(client);
        });
    }
}
