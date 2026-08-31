package com.jones.watermelonmod.client.goggles;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.mixin.client.GameRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Selects a pipeline from the HEAD item without knowing any individual DSP effect. */
public final class GogglesPostProcessingController {
    private static Identifier activePostEffect;

    private GogglesPostProcessingController() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.gameRenderer == null) {
            clearOwnedEffect(client);
            return;
        }

        Optional<Identifier> requestedEffect = GogglesEquipment.equippedGoggles(client.player)
                .flatMap(GogglesPostProcessingController::resolvePostEffect);
        if (requestedEffect.isPresent()) {
            apply(client, requestedEffect.get());
        } else {
            clearOwnedEffect(client);
        }
    }

    private static Optional<Identifier> resolvePostEffect(ItemStack headStack) {
        if (!(headStack.getItem() instanceof GogglesItem goggles)) {
            return Optional.empty();
        }
        return GogglesPipelineRegistry.get(goggles.pipeline().id())
                .flatMap(pipeline -> pipeline.postEffect(GogglesSettingsService.get(headStack)));
    }

    private static void apply(Minecraft client, Identifier requestedEffect) {
        if (!requestedEffect.equals(activePostEffect) || !requestedEffect.equals(client.gameRenderer.currentPostEffect())) {
            ((GameRendererInvoker) client.gameRenderer).watermelonmod$setPostEffect(requestedEffect);
            activePostEffect = requestedEffect;
        }
    }

    private static void clearOwnedEffect(Minecraft client) {
        if (activePostEffect != null && activePostEffect.equals(client.gameRenderer.currentPostEffect())) {
            client.gameRenderer.clearPostEffect();
        }
        activePostEffect = null;
    }
}
