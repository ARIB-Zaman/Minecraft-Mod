package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/** Uploads per-item greyscale intensity into the post-process UBO every frame. */
@Mixin(PostPass.class)
public abstract class PostPassMixin {
    @Shadow private String name;
    @Shadow private Map<String, GpuBuffer> customUniforms;
    @Unique private GpuBuffer watermelonmod$greyscaleBuffer;
    @Unique private final ByteBuffer watermelonmod$intensityData = ByteBuffer.allocateDirect(Float.BYTES).order(ByteOrder.nativeOrder());

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void watermelonmod$updateGreyscaleUniform(CallbackInfo ci) {
        if (!name.contains("watermelonmod:greyscale/0")) return;
        float intensity = Minecraft.getInstance().player == null ? 0.0F
                : GogglesEquipment.equippedGoggles(Minecraft.getInstance().player)
                .filter(stack -> stack.getItem() instanceof GreyscaleGogglesItem)
                .map(stack -> GogglesSettingsService.get(stack).value("intensity", 1.0F))
                .orElse(0.0F);
        GpuBuffer buffer = customUniforms.get("GreyscaleConfig");
        if (buffer == null) return;
        // Vanilla's JSON-created UBO is immutable. Replace it once with an
        // equivalent UBO that explicitly permits the per-frame GPU upload.
        if (watermelonmod$greyscaleBuffer == null || watermelonmod$greyscaleBuffer.isClosed()) {
            watermelonmod$greyscaleBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "watermelonmod GreyscaleConfig",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    buffer.size()
            );
            customUniforms.put("GreyscaleConfig", watermelonmod$greyscaleBuffer);
            buffer.close();
        }
        watermelonmod$intensityData.clear();
        watermelonmod$intensityData.putFloat(intensity).flip();
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(
                watermelonmod$greyscaleBuffer.slice(0, Float.BYTES), watermelonmod$intensityData
        );
    }
}
