package com.jones.watermelonmod.client.fft;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.client.veil.VeilClientState;
import com.jones.watermelonmod.client.veil.VeilKernel;
import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettings;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.BandPassGogglesItem;
import com.jones.watermelonmod.item.custom.FrequencyFilterGogglesItem;
import com.jones.watermelonmod.item.custom.HighPassGogglesItem;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Fixed-size, fragment-shader 2D FFT. The analysis resolution keeps the
 * effect affordable while all intermediate complex values remain in
 * RGBA32_FLOAT targets rather than Minecraft's usual RGBA8 post targets.
 *
 * <p>Per frame: forward FFT, then the Veil's degradation (G = H*F + N), a
 * spectrum snapshot of what reaches the eye, the Veil goggles' restoration
 * filter or another goggles' frequency filter, and the inverse FFT.</p>
 */
public final class GpuFftProcessor {
    private static final float[] NO_RESTORE = {0.0F, 0.0F, 0.0F, 0.0F};
    private static final float VEIL_SPECTRUM_OPACITY = 0.85F;
    /** The Veil overlay shows |f| up to this many cycles per reference pixel at every quality. */
    private static final float VEIL_SPECTRUM_BAND = 0.25F;
    /** Automatic quality drops to LOW after this many client ticks (5 s) below the frame-rate floor. */
    private static final int LOW_FPS_FLOOR = 25;
    private static final int LOW_FPS_TICKS = 100;

    private static boolean active;
    private static FilterMode filterMode = FilterMode.NONE;
    private static float cutoff = 0.20F;
    private static float spectrumOpacity;
    private static float spectrumBand;
    private static float bandLowCutoff = 0.05F;
    private static float bandHighCutoff = 0.25F;
    private static VeilKernel degradation;
    private static float[] restoreKernel = NO_RESTORE;
    private static float[] restoreSettings = NO_RESTORE;
    private static FftQuality quality = FftQuality.HIGH;
    private static boolean automaticQuality = true;
    private static int lowFpsTicks;
    private static Targets targets;
    private static FftFullscreenPasses passes;

    private GpuFftProcessor() {
    }

    /** Called on the client tick; the actual GPU work is done on render thread. */
    public static void tick(Minecraft client) {
        filterMode = FilterMode.NONE;
        spectrumOpacity = 0.0F;
        spectrumBand = 0.0F;
        restoreSettings = NO_RESTORE;
        degradation = null;
        if (client.player == null) {
            active = false;
            return;
        }

        degradation = VeilClientState.degradation().orElse(null);
        GogglesEquipment.equippedGoggles(client.player).ifPresent(GpuFftProcessor::readGoggles);
        active = degradation != null || filterMode != FilterMode.NONE || spectrumOpacity > 0.0F;
        guardFrameRate(client);
    }

    public static void setQuality(FftQuality requested) {
        quality = requested;
        automaticQuality = false;
        lowFpsTicks = 0;
    }

    public static void setAutomaticQuality() {
        quality = FftQuality.HIGH;
        automaticQuality = true;
        lowFpsTicks = 0;
    }

    private static void readGoggles(ItemStack stack) {
        GogglesSettings settings = GogglesSettingsService.get(stack);
        if (stack.getItem() instanceof FrequencyFilterGogglesItem) {
            filterMode = FilterMode.GAUSSIAN_LOW_PASS;
            cutoff = settings.value("cutoff", 0.20F);
            spectrumOpacity = settings.value("spectrum_opacity", 0.75F);
        } else if (stack.getItem() instanceof HighPassGogglesItem) {
            filterMode = FilterMode.GAUSSIAN_HIGH_PASS;
            cutoff = settings.value("cutoff", 0.10F);
            spectrumOpacity = settings.value("spectrum_opacity", 0.75F);
        } else if (stack.getItem() instanceof BandPassGogglesItem) {
            filterMode = FilterMode.HARD_BAND_PASS;
            float first = settings.value("low_cutoff", 0.05F);
            float second = settings.value("high_cutoff", 0.25F);
            bandLowCutoff = Math.min(first, second);
            bandHighCutoff = Math.max(first, second);
            spectrumOpacity = settings.value("spectrum_opacity", 0.75F);
        } else if (stack.getItem() instanceof VeilGogglesItem) {
            spectrumOpacity = VEIL_SPECTRUM_OPACITY;
            spectrumBand = VEIL_SPECTRUM_BAND;
            int mode = Math.round(settings.value(VeilGogglesItem.MODE, VeilGogglesItem.MODE_OFF));
            if (mode != VeilGogglesItem.MODE_OFF) {
                VeilKernel estimate = new VeilKernel(
                        VeilKernel.Type.fromIndex(Math.round(settings.value(VeilGogglesItem.KERNEL, 1.0F))),
                        settings.value(VeilGogglesItem.SIZE, 16.0F),
                        settings.value(VeilGogglesItem.ANGLE, 0.0F),
                        0.0F);
                restoreKernel = estimate.shaderKernel();
                restoreSettings = new float[]{mode, settings.value(VeilGogglesItem.EPSILON, 0.1F),
                        (float) Math.pow(10.0, settings.value(VeilGogglesItem.LOG_K, -2.0F)), 0.0F};
            }
        }
    }

    /** Drops to the cheaper FFT when the frame rate stays low, unless the player chose a quality. */
    private static void guardFrameRate(Minecraft client) {
        if (!active || !automaticQuality || quality == FftQuality.LOW) {
            lowFpsTicks = 0;
            return;
        }
        int fps = client.getFps();
        lowFpsTicks = fps > 0 && fps < LOW_FPS_FLOOR ? lowFpsTicks + 1 : 0;
        if (lowFpsTicks >= LOW_FPS_TICKS) {
            quality = FftQuality.LOW;
            lowFpsTicks = 0;
            client.player.sendSystemMessage(Component.translatable("message.watermelonmod.fft.quality_lowered"));
        }
    }

    /** Invoked from GameRenderer after the vanilla post-chain, before the GUI. */
    public static void renderIfActive(RenderTarget mainTarget) {
        if (!active || mainTarget.width <= 0 || mainTarget.height <= 0) return;
        RenderSystem.assertOnRenderThread();
        ensureResources();
        int bitsX = targets.quality.log2Width();
        int bitsY = targets.quality.log2Height();

        passes.one("pack_rg", mainTarget, targets.rgA, bitsX, bitsY, 0, 0);
        passes.one("pack_b", mainTarget, targets.bA, bitsX, bitsY, 0, 0);

        RenderTarget rg = transform(targets.rgA, targets.rgB, targets.rgA, targets.rgB, false, bitsX, bitsY);
        RenderTarget blue = transform(targets.bA, targets.bB, targets.bA, targets.bB, false, bitsX, bitsY);
        if (degradation != null) {
            float[] kernel = degradation.shaderKernel();
            rg = passes.veil("veil_degrade", rg, other(rg, targets.rgA, targets.rgB), 0, kernel, NO_RESTORE);
            blue = passes.veil("veil_degrade", blue, other(blue, targets.bA, targets.bB), 1, kernel, NO_RESTORE);
        }
        // Capture what reaches the eye: after the Veil, before any restoration or filtering.
        if (spectrumOpacity > 0.0F) {
            // zoom = (full band) / (shown band), both in cycles per FFT pixel.
            float zoom = spectrumBand > 0.0F ? 0.5F * targets.quality.width() / (1024.0F * spectrumBand) : 1.0F;
            passes.captureSpectrum(rg, blue, targets.spectrum, zoom);
        }
        if (restoreSettings != NO_RESTORE) {
            rg = passes.veil("deconvolve", rg, other(rg, targets.rgA, targets.rgB), 0, restoreKernel, restoreSettings);
            blue = passes.veil("deconvolve", blue, other(blue, targets.bA, targets.bB), 1, restoreKernel, restoreSettings);
        }
        if (filterMode != FilterMode.NONE) {
            String filterShader = filterMode == FilterMode.HARD_BAND_PASS ? "band_pass" : "filter";
            float filterLow = filterMode == FilterMode.HARD_BAND_PASS ? bandLowCutoff : cutoff;
            // For the "filter" shader this second slot doubles as the invert flag (high-pass when > 0.5);
            // for "band_pass" it is genuinely the upper cutoff. The two shaders never share an invocation.
            float filterHigh = filterMode == FilterMode.HARD_BAND_PASS ? bandHighCutoff
                    : filterMode == FilterMode.GAUSSIAN_HIGH_PASS ? 1.0F : 0.0F;
            rg = passes.one(filterShader, rg, other(rg, targets.rgA, targets.rgB), filterLow, filterHigh, 0, 0);
            blue = passes.one(filterShader, blue, other(blue, targets.bA, targets.bB), filterLow, filterHigh, 0, 0);
        }
        rg = passes.one("reorder", rg, other(rg, targets.rgA, targets.rgB), bitsX, bitsY, 0, 0);
        blue = passes.one("reorder", blue, other(blue, targets.bA, targets.bB), bitsX, bitsY, 0, 0);
        rg = transform(rg, other(rg, targets.rgA, targets.rgB), targets.rgA, targets.rgB, true, bitsX, bitsY);
        blue = transform(blue, other(blue, targets.bA, targets.bB), targets.bA, targets.bB, true, bitsX, bitsY);

        passes.two("unpack", rg, blue, targets.reconstructed, 0, 0, 0, 0);
        passes.one("composite", targets.reconstructed, mainTarget, 0, 0, 0, 0);
        if (spectrumOpacity > 0.0F) passes.overlaySpectrum(targets.spectrum, mainTarget, spectrumOpacity);
    }

    private static RenderTarget transform(RenderTarget source, RenderTarget destination, RenderTarget first, RenderTarget second,
                                          boolean inverse, int bitsX, int bitsY) {
        for (int stage = 0; stage < bitsX; stage++) {
            source = passes.one("butterfly", source, destination, stage, 0, inverse ? 1 : 0, 0);
            destination = other(source, first, second);
        }
        for (int stage = 0; stage < bitsY; stage++) {
            source = passes.one("butterfly", source, destination, stage, 1, inverse ? 1 : 0, 0);
            destination = other(source, first, second);
        }
        return source;
    }

    private static RenderTarget other(RenderTarget current, RenderTarget first, RenderTarget second) {
        return current == first ? second : first;
    }

    private static void ensureResources() {
        if (passes == null) {
            passes = new FftFullscreenPasses();
        }
        if (targets == null || targets.quality != quality) {
            if (targets != null) {
                targets.destroy();
            }
            targets = new Targets(quality);
            WatermelonMod.LOGGER.info("Created {}x{} floating-point FFT render targets", quality.width(), quality.height());
        }
    }

    private static final class Targets {
        final FftQuality quality;
        final TextureTarget rgA;
        final TextureTarget rgB;
        final TextureTarget bA;
        final TextureTarget bB;
        final TextureTarget reconstructed;
        // A compact, persistent, square snapshot of the forward spectrum for the PIP overlay.
        final TextureTarget spectrum = new TextureTarget("watermelonmod FFT spectrum", 256, 256, false, GpuFormat.RGBA8_UNORM);

        Targets(FftQuality quality) {
            this.quality = quality;
            rgA = target("FFT RG ping", GpuFormat.RGBA32_FLOAT);
            rgB = target("FFT RG pong", GpuFormat.RGBA32_FLOAT);
            bA = target("FFT B ping", GpuFormat.RGBA32_FLOAT);
            bB = target("FFT B pong", GpuFormat.RGBA32_FLOAT);
            reconstructed = target("FFT reconstructed", GpuFormat.RGBA8_UNORM);
        }

        private TextureTarget target(String name, GpuFormat format) {
            return new TextureTarget("watermelonmod " + name, quality.width(), quality.height(), false, format);
        }

        void destroy() {
            for (TextureTarget target : new TextureTarget[]{rgA, rgB, bA, bB, reconstructed, spectrum}) {
                target.destroyBuffers();
            }
        }
    }

    private enum FilterMode {
        NONE,
        GAUSSIAN_LOW_PASS,
        GAUSSIAN_HIGH_PASS,
        HARD_BAND_PASS
    }
}