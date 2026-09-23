package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.client.echofunnel.EchoFunnelScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps movement bindings active only while the Echo Funnel interface is open.
 *
 * <p>Vanilla intentionally stops updating gameplay key mappings whenever a
 * {@code Screen} is active. The Funnel is an in-combat interface, so its
 * movement bindings are forwarded without changing input behavior for any
 * other screen. Mouse input remains owned by the screen.</p>
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerEchoFunnelMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void watermelonmod$forwardEchoFunnelMovement(
        long handle,
        int action,
        KeyEvent event,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        if (!(client.gui.screen() instanceof EchoFunnelScreen)) {
            return;
        }

        Options options = client.options;
        if (!isMovementBinding(options, event)) {
            return;
        }

        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, action != 0);
    }

    private static boolean isMovementBinding(Options options, KeyEvent event) {
        return options.keyUp.matches(event)
            || options.keyDown.matches(event)
            || options.keyLeft.matches(event)
            || options.keyRight.matches(event)
            || options.keyJump.matches(event)
            || options.keyShift.matches(event)
            || options.keySprint.matches(event);
    }
}
