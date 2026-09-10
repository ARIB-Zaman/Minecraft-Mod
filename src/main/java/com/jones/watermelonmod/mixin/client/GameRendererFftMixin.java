package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.client.fft.GpuFftProcessor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Runs after the world/post-chain and before Minecraft draws its GUI. */
@Mixin(GameRenderer.class)
public abstract class GameRendererFftMixin {
    @Shadow @Final private RenderTarget mainRenderTarget;

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V", shift = At.Shift.BEFORE)
    )
    private void watermelonmod$applyFrequencyFilter(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        GpuFftProcessor.renderIfActive(mainRenderTarget);
    }
}
