package com.jones.watermelonmod;

import com.jones.watermelonmod.client.entity.RadiationWardenRenderer;
import com.jones.watermelonmod.client.fft.GpuFftProcessor;
import com.jones.watermelonmod.client.goggles.GogglesPipelineRegistry;
import com.jones.watermelonmod.client.goggles.GogglesPostProcessingController;
import com.jones.watermelonmod.client.goggles.PostEffectGogglesPipeline;
import com.jones.watermelonmod.client.sonar.SonarController;
import com.jones.watermelonmod.client.sonar.SonarHud;
import com.jones.watermelonmod.client.sonar.SonarState;
import com.jones.watermelonmod.client.veil.VeilClientState;
import com.jones.watermelonmod.client.veil.VeilCommands;
import com.jones.watermelonmod.client.veil.VeilEmitterClientHandler;
import com.jones.watermelonmod.client.veil.VeilTrialState;
import com.jones.watermelonmod.client.veil.VeilTrials;
import com.jones.watermelonmod.client.veil.VeilTuningState;
import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import com.jones.watermelonmod.client.workbench.WorkbenchScreen;
import com.jones.watermelonmod.entity.ModEntities;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.menu.ModMenus;
import com.jones.watermelonmod.network.VeilBlindPayload;
import com.jones.watermelonmod.network.VeilRecomposedPayload;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.sounds.SoundEvents;

import java.util.Locale;

/** Client-only bridge between equipped goggles and Minecraft's post-effect renderer. */
public final class WatermelonModClient implements ClientModInitializer {
    private static final KeyMapping SONAR_PING_KEY = new KeyMapping(
            "key.watermelonmod.sonar_ping", InputConstants.KEY_G, KeyMapping.Category.GAMEPLAY
    );
    private static final KeyMapping VEIL_CYCLE_PARAMETER_KEY = new KeyMapping(
            "key.watermelonmod.veil_cycle_parameter", InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY
    );
    /** How close (in degrees, accounting for the 180°-periodic symmetry of a motion blur's direction) counts as a pass. */
    private static final float TRIAL_TOLERANCE_DEGREES = 15.0F;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.RADIATION_WARDEN, RadiationWardenRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SILENCE_DOME, NoopRenderer::new);
        EntityRendererRegistry.register(ModEntities.FREEZE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.DAMAGE_BREEZE_PROJECTILE, ThrownItemRenderer::new);
        MenuScreens.register(ModMenus.WORKBENCH, WorkbenchScreen::new);
        VeilCommands.register();
        ClientPlayNetworking.registerGlobalReceiver(VeilBlindPayload.TYPE, (payload, context) ->
                VeilTrials.startRandomAngle(context.player(), payload.seconds(), true));
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
        KeyMappingHelper.registerKeyMapping(VEIL_CYCLE_PARAMETER_KEY);
        HudElementRegistry.addLast(WatermelonMod.id("sonar_hud"), new SonarHud());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GogglesPostProcessingController.tick(client);
            VeilEmitterClientHandler.tick(client);
            GpuFftProcessor.tick(client);
            if (VEIL_CYCLE_PARAMETER_KEY.consumeClick() && client.player != null) {
                GogglesEquipment.equippedGoggles(client.player)
                        .filter(stack -> stack.getItem() instanceof VeilGogglesItem)
                        .ifPresent(stack -> {
                            var pipeline = ((GogglesItem) stack.getItem()).pipeline();
                            VeilTuningState.cycle(pipeline);
                            var selected = VeilTuningState.selected(pipeline);
                            client.player.sendOverlayMessage(Component.translatable("message.watermelonmod.veil.selected",
                                    Component.translatable("gui.watermelonmod.workbench.veil." + selected.key())));
                        });
            }
            tickVeilTrial(client);
            SonarState.advanceTick();
            if (SONAR_PING_KEY.consumeClick()) {
                SonarController.ping(client);
            }
            SonarController.tick(client);
        });
    }

    /** Shows the trial countdown each tick, then scores it the moment time runs out. */
    private static void tickVeilTrial(net.minecraft.client.Minecraft client) {
        if (!VeilTrialState.isActive() || client.player == null) {
            return;
        }
        if (VeilTrialState.expired()) {
            evaluateVeilTrial(client);
            return;
        }
        GogglesEquipment.equippedGoggles(client.player)
                .filter(stack -> stack.getItem() instanceof VeilGogglesItem)
                .ifPresent(stack -> {
                    float currentAngle = GogglesSettingsService.get(stack).value(VeilGogglesItem.ANGLE, 0.0F);
                    client.player.sendOverlayMessage(Component.translatable("message.watermelonmod.veil.trial_countdown",
                            String.format(Locale.ROOT, "%.1f", VeilTrialState.secondsRemaining()), Math.round(currentAngle),
                            Math.round(VeilTrialState.distanceTo(currentAngle))));
                });
    }

    private static void evaluateVeilTrial(net.minecraft.client.Minecraft client) {
        boolean combatTriggered = VeilTrialState.isCombatTriggered();
        VeilTrialState.stop();
        GogglesEquipment.equippedGoggles(client.player)
                .filter(stack -> stack.getItem() instanceof VeilGogglesItem)
                .ifPresentOrElse(stack -> {
                    float currentAngle = GogglesSettingsService.get(stack).value(VeilGogglesItem.ANGLE, 0.0F);
                    float target = VeilTrialState.targetAngle();
                    float angularDistance = VeilTrialState.distanceTo(currentAngle);
                    boolean success = angularDistance <= TRIAL_TOLERANCE_DEGREES;
                    // Otherwise the goggles keep "restoring" a blur that no longer
                    // exists once the trial ends, distorting an already-clear view.
                    GogglesSettingsService.setParameter(stack, VeilGogglesItem.MODE, VeilGogglesItem.MODE_OFF);
                    if (success) {
                        VeilClientState.clear();
                        client.player.sendSystemMessage(Component.translatable("message.watermelonmod.veil.trial_success",
                                Math.round(target), Math.round(angularDistance)));
                        client.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.5F);
                        if (combatTriggered) {
                            ClientPlayNetworking.send(new VeilRecomposedPayload(Math.round(angularDistance)));
                        }
                    } else {
                        client.player.sendSystemMessage(Component.translatable("message.watermelonmod.veil.trial_fail",
                                Math.round(target), Math.round(angularDistance)));
                        client.player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.8F);
                    }
                }, VeilClientState::clear);
    }
}
