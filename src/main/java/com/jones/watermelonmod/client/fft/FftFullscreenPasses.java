package com.jones.watermelonmod.client.fft;

import com.jones.watermelonmod.WatermelonMod;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/** Minimal direct renderer for the FFT shaders; it avoids JSON post-chain RGBA8 targets. */
final class FftFullscreenPasses {
    private final Map<String, RenderPipeline> oneInput = new HashMap<>();
    private final Map<String, RenderPipeline> twoInput = new HashMap<>();
    private final GpuBuffer samplerInfo;
    private final GpuBuffer config;
    private final ByteBuffer data = ByteBuffer.allocateDirect(4 * Float.BYTES).order(ByteOrder.nativeOrder());

    FftFullscreenPasses() {
        for (String name : new String[]{"pack_rg", "pack_b", "butterfly", "filter", "band_pass", "reorder"}) {
            oneInput.put(name, create(name, false, GpuFormat.RGBA32_FLOAT));
        }
        oneInput.put("composite", create("composite", false, GpuFormat.RGBA8_UNORM));
        oneInput.put("spectrum_overlay", createOverlay("spectrum_overlay"));
        twoInput.put("unpack", create("unpack", true, GpuFormat.RGBA8_UNORM));
        twoInput.put("spectrum", create("spectrum", true, GpuFormat.RGBA8_UNORM));
        samplerInfo = buffer("FFT sampler info");
        config = buffer("FFT config");
    }

    RenderTarget one(String shader, RenderTarget input, RenderTarget output, float x, float y, float z, float w) {
        upload(samplerInfo, output.width, output.height, input.width, input.height);
        upload(config, x, y, z, w);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "watermelonmod FFT " + shader,
                output.getColorTextureView(), Optional.empty(),
                output.useDepth ? output.getDepthTextureView() : null, OptionalDouble.empty())) {
            pass.setPipeline(oneInput.get(shader));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfo);
            pass.setUniform("FftConfig", config);
            pass.bindTexture("InSampler", input.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(3, 1, 0, 0);
        }
        return output;
    }

    void two(String shader, RenderTarget rg, RenderTarget blue, RenderTarget output, float x, float y, float z, float w) {
        upload(samplerInfo, output.width, output.height, rg.width, rg.height);
        upload(config, x, y, z, w);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "watermelonmod FFT " + shader,
                output.getColorTextureView(), Optional.empty(),
                output.useDepth ? output.getDepthTextureView() : null, OptionalDouble.empty())) {
            pass.setPipeline(twoInput.get(shader));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SamplerInfo", samplerInfo);
            pass.setUniform("FftConfig", config);
            pass.bindTexture("RGSampler", rg.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.bindTexture("BSampler", blue.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(3, 1, 0, 0);
        }
    }

    /** Converts packed RGB complex coefficients into a persistent display-sized spectrum. */
    void captureSpectrum(RenderTarget rgSpectrum, RenderTarget blueSpectrum, RenderTarget spectrumOutput) {
        two("spectrum", rgSpectrum, blueSpectrum, spectrumOutput, 0, 0, 0, 0);
    }

    /** Alpha-blends a captured spectrum into a corner of the already-composited scene. */
    void overlaySpectrum(RenderTarget spectrumInput, RenderTarget sceneOutput, float opacity) {
        one("spectrum_overlay", spectrumInput, sceneOutput, opacity, 0, 0, 0);
    }

    private static RenderPipeline create(String shader, boolean twoInputs, GpuFormat outputFormat) {
        BindGroupLayout.Builder bindings = BindGroupLayout.builder();
        if (twoInputs) {
            bindings.withSampler("RGSampler").withSampler("BSampler");
        } else {
            bindings.withSampler("InSampler");
        }
        bindings.withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER).withUniform("FftConfig", UniformType.UNIFORM_BUFFER);
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
                .withFragmentShader(WatermelonMod.id("fft/" + shader))
                .withLocation(WatermelonMod.id("fft/" + shader))
                .withColorTargetState(new ColorTargetState(Optional.empty(), outputFormat, ColorTargetState.WRITE_ALL))
                .withBindGroupLayout(bindings.build())
                .build();
        if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            throw new IllegalStateException("Could not compile FFT shader " + shader);
        }
        return pipeline;
    }

    private static RenderPipeline createOverlay(String shader) {
        BindGroupLayout bindings = BindGroupLayout.builder()
                .withSampler("InSampler")
                .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER)
                .withUniform("FftConfig", UniformType.UNIFORM_BUFFER)
                .build();
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
                .withFragmentShader(WatermelonMod.id("fft/" + shader))
                .withLocation(WatermelonMod.id("fft/" + shader))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withBindGroupLayout(bindings)
                .build();
        if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
            throw new IllegalStateException("Could not compile FFT shader " + shader);
        }
        return pipeline;
    }

    private static GpuBuffer buffer(String name) {
        return RenderSystem.getDevice().createBuffer(() -> "watermelonmod " + name,
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 4 * Float.BYTES);
    }

    private void upload(GpuBuffer target, float a, float b, float c, float d) {
        data.clear().putFloat(a).putFloat(b).putFloat(c).putFloat(d).flip();
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(target.slice(), data);
    }
}
