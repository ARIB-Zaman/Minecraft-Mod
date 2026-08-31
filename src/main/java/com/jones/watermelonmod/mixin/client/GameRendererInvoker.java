package com.jones.watermelonmod.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Uses the current GameRenderer post-chain loader rather than direct OpenGL calls. */
@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("setPostEffect")
    void watermelonmod$setPostEffect(Identifier postEffectId);
}
