package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.client.echofunnel.EchoFunnelScreen;
import com.jones.watermelonmod.item.ModItems;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Diverts the normal left-click attack action into the Echo Funnel interface. */
@Mixin(Minecraft.class)
public abstract class MinecraftEchoFunnelMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void watermelonmod$openEchoFunnel(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.player.getMainHandItem().is(ModItems.ECHO_FUNNEL)) {
            client.gui.setScreen(new EchoFunnelScreen());
            cir.setReturnValue(true);
        }
    }
}
