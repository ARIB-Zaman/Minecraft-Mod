package com.jones.watermelonmod.mixin.client;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GreyscaleGogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
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

/** Uploads per-item post-effect settings into their post-process UBO every frame. */
@Mixin(PostPass.class)
public abstract class PostPassMixin {
    @Shadow private String name;
    @Shadow private Map<String, GpuBuffer> customUniforms;
    @Unique private GpuBuffer watermelonmod$dynamicBuffer;
    @Unique private final ByteBuffer watermelonmod$uniformData = ByteBuffer.allocateDirect(9 * Float.BYTES).order(ByteOrder.nativeOrder());

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void watermelonmod$updateGreyscaleUniform(CallbackInfo ci) {
        if (!name.contains("watermelonmod:greyscale/0")) return;
        float intensity = Minecraft.getInstance().player == null ? 0.0F
                : GogglesEquipment.equippedGoggles(Minecraft.getInstance().player)
                .filter(stack -> stack.getItem() instanceof GreyscaleGogglesItem)
                .map(stack -> GogglesSettingsService.get(stack).value("intensity", 1.0F))
                .orElse(0.0F);
        uploadFloats("GreyscaleConfig", intensity);
    }

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void watermelonmod$updateEdgeUniform(CallbackInfo ci) {
        if (!name.contains("watermelonmod:edge_detection/0")) return;
        float[] kernel = Minecraft.getInstance().player == null ? new float[9]
                : GogglesEquipment.equippedGoggles(Minecraft.getInstance().player)
                .filter(stack -> stack.getItem() instanceof EdgeDetectionGogglesItem)
                .map(stack -> {
                    float[] values = new float[9];
                    for (int index = 0; index < values.length; index++) {
                        values[index] = GogglesSettingsService.get(stack).value(EdgeDetectionGogglesItem.coefficientKey(index), 0.0F);
                    }
                    return values;
                }).orElseGet(() -> new float[9]);
        uploadFloats("EdgeConfig", kernel);
    }

    @Unique
    private void uploadFloats(String uniformName, float... values) {
        GpuBuffer buffer = customUniforms.get(uniformName);
        if (buffer == null) return;
        // Vanilla's JSON-created UBO is immutable. Replace it once with an
        // equivalent UBO that explicitly permits the per-frame GPU upload.
        if (watermelonmod$dynamicBuffer == null || watermelonmod$dynamicBuffer.isClosed()) {
            watermelonmod$dynamicBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "watermelonmod " + uniformName,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    buffer.size()
            );
            customUniforms.put(uniformName, watermelonmod$dynamicBuffer);
            buffer.close();
        }
        watermelonmod$uniformData.clear();
        for (float value : values) watermelonmod$uniformData.putFloat(value);
        watermelonmod$uniformData.flip();
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(
                watermelonmod$dynamicBuffer.slice(0, (long)values.length * Float.BYTES), watermelonmod$uniformData
        );
    }
}
