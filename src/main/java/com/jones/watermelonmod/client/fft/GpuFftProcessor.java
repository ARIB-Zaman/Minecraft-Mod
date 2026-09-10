package com.jones.watermelonmod.client.fft;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.FrequencyFilterGogglesItem;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

/**
 * Fixed-size, fragment-shader 2D FFT.  The small analysis resolution keeps
 * the demonstration affordable while all intermediate complex values remain
 * in RGBA32_FLOAT targets rather than Minecraft's usual RGBA8 post targets.
 */
public final class GpuFftProcessor {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 512;

    private static boolean active;
    private static float cutoff = 0.20F;
    private static Targets targets;
    private static FftFullscreenPasses passes;

    private GpuFftProcessor() {
    }

    /** Called on the client tick; the actual GPU work is done on render thread. */
    public static void tick(Minecraft client) {
        active = client.player != null && GogglesEquipment.equippedGoggles(client.player)
                .filter(stack -> stack.getItem() instanceof FrequencyFilterGogglesItem)
                .map(stack -> {
                    cutoff = GogglesSettingsService.get(stack).value("cutoff", 0.20F);
                    return true;
                })
                .orElse(false);
    }

    /** Invoked from GameRenderer after the vanilla post-chain, before the GUI. */
    public static void renderIfActive(RenderTarget mainTarget) {
        if (!active || mainTarget.width <= 0 || mainTarget.height <= 0) return;
        RenderSystem.assertOnRenderThread();
        ensureResources();

        passes.one("pack_rg", mainTarget, targets.rgA, 0, 0, 0, 0);
        passes.one("pack_b", mainTarget, targets.bA, 0, 0, 0, 0);

        RenderTarget rg = transform(targets.rgA, targets.rgB, targets.rgA, targets.rgB, false);
        RenderTarget blue = transform(targets.bA, targets.bB, targets.bA, targets.bB, false);
        rg = passes.one("filter", rg, other(rg, targets.rgA, targets.rgB), cutoff, 0, 0, 0);
        blue = passes.one("filter", blue, other(blue, targets.bA, targets.bB), cutoff, 0, 0, 0);
        rg = passes.one("reorder", rg, other(rg, targets.rgA, targets.rgB), 0, 0, 0, 0);
        blue = passes.one("reorder", blue, other(blue, targets.bA, targets.bB), 0, 0, 0, 0);
        rg = transform(rg, other(rg, targets.rgA, targets.rgB), targets.rgA, targets.rgB, true);
        blue = transform(blue, other(blue, targets.bA, targets.bB), targets.bA, targets.bB, true);

        passes.two("unpack", rg, blue, targets.reconstructed, 0, 0, 0, 0);
        passes.one("composite", targets.reconstructed, mainTarget, 0, 0, 0, 0);
    }

    private static RenderTarget transform(RenderTarget source, RenderTarget destination, RenderTarget first, RenderTarget second, boolean inverse) {
        for (int stage = 0; stage < 10; stage++) {
            source = passes.one("butterfly", source, destination, stage, 0, inverse ? 1 : 0, 0);
            destination = other(source, first, second);
        }
        for (int stage = 0; stage < 9; stage++) {
            source = passes.one("butterfly", source, destination, stage, 1, inverse ? 1 : 0, 0);
            destination = other(source, first, second);
        }
        return source;
    }

    private static RenderTarget other(RenderTarget current, RenderTarget first, RenderTarget second) {
        return current == first ? second : first;
    }

    private static void ensureResources() {
        if (targets == null) {
            targets = new Targets();
            passes = new FftFullscreenPasses();
            WatermelonMod.LOGGER.info("Created 1024x512 floating-point FFT render targets");
        }
    }

    private static final class Targets {
        final TextureTarget rgA = target("FFT RG ping", GpuFormat.RGBA32_FLOAT);
        final TextureTarget rgB = target("FFT RG pong", GpuFormat.RGBA32_FLOAT);
        final TextureTarget bA = target("FFT B ping", GpuFormat.RGBA32_FLOAT);
        final TextureTarget bB = target("FFT B pong", GpuFormat.RGBA32_FLOAT);
        final TextureTarget reconstructed = target("FFT reconstructed", GpuFormat.RGBA8_UNORM);

        private static TextureTarget target(String name, GpuFormat format) {
            return new TextureTarget("watermelonmod " + name, WIDTH, HEIGHT, false, format);
        }
    }
}
