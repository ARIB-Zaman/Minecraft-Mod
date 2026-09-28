package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.client.veil.VeilTrialState;
import com.jones.watermelonmod.client.veil.VeilTuningState;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import com.jones.watermelonmod.network.AdjustGogglesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Sneak + scroll nudges the currently selected Veil Goggles parameter live,
 * so tuning them no longer requires opening the workbench for every change.
 * Mirrors {@code KeyboardHandlerEchoFunnelMixin}'s early-injection style.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerVeilMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void watermelonmod$adjustVeilGoggles(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.screen() != null || !client.player.isShiftKeyDown() || yoffset == 0.0) {
            return;
        }

        Optional<ItemStack> veilGoggles = GogglesEquipment.equippedGoggles(client.player)
                .filter(stack -> stack.getItem() instanceof VeilGogglesItem);
        if (veilGoggles.isEmpty()) {
            return;
        }

        ItemStack stack = veilGoggles.get();
        // During a timed trial, scrolling always targets angle, regardless of
        // whatever the player last selected with V — keeps the test focused.
        GogglesParameter parameter = VeilTrialState.isActive()
                ? ((GogglesItem) stack.getItem()).pipeline().parameters().get(VeilGogglesItem.ANGLE)
                : VeilTuningState.selected(((GogglesItem) stack.getItem()).pipeline());
        float step = VeilGogglesItem.stepFor(parameter.key()) * (float) Math.signum(yoffset);
        float current = GogglesSettingsService.get(stack).value(parameter.key(), parameter.defaultValue());
        // Predicted locally for an instant-feeling knob; the server applies and
        // persists the same change, following the Echo Funnel's request pattern.
        GogglesSettingsService.setParameter(stack, parameter.key(), current + step);
        ClientPlayNetworking.send(new AdjustGogglesPayload(parameter.key(), step));

        float updated = GogglesSettingsService.get(stack).value(parameter.key(), parameter.defaultValue());
        client.player.sendOverlayMessage(Component.translatable("message.watermelonmod.veil.tuning",
                Component.translatable("gui.watermelonmod.workbench.veil." + parameter.key()),
                VeilGogglesItem.describe(parameter.key(), updated)));
        ci.cancel();
    }
}
