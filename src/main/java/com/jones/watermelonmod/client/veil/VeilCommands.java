package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.client.fft.FftQuality;
import com.jones.watermelonmod.client.fft.GpuFftProcessor;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Client-only test command for the Veil:
 * <pre>
 * /veil gaussian &lt;sigma&gt; [noise]
 * /veil motion &lt;length&gt; &lt;angle&gt; [noise]
 * /veil defocus &lt;diameter&gt; [noise]
 * /veil off
 * /veil quality high|low|auto
 * </pre>
 */
public final class VeilCommands {
    private static final float DEFAULT_NOISE = 0.003F;
    private static final float MAX_NOISE = 0.05F;

    private VeilCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("veil")
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("off").executes(context -> {
                            VeilClientState.clear();
                            context.getSource().sendFeedback(Component.translatable("command.watermelonmod.veil.off"));
                            return 1;
                        }))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("gaussian")
                                .then(withNoise(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument("sigma", FloatArgumentType.floatArg(0.5F, 32.0F)),
                                        (context, noise) -> apply(context, VeilKernel.Type.GAUSSIAN, FloatArgumentType.getFloat(context, "sigma"), 0.0F, noise))))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("motion")
                                .then(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument("length", FloatArgumentType.floatArg(1.0F, 64.0F))
                                        .then(withNoise(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument("angle", FloatArgumentType.floatArg(0.0F, 180.0F)),
                                                (context, noise) -> apply(context, VeilKernel.Type.MOTION, FloatArgumentType.getFloat(context, "length"),
                                                        FloatArgumentType.getFloat(context, "angle"), noise)))))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("defocus")
                                .then(withNoise(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument("diameter", FloatArgumentType.floatArg(1.0F, 64.0F)),
                                        (context, noise) -> apply(context, VeilKernel.Type.DEFOCUS, FloatArgumentType.getFloat(context, "diameter"), 0.0F, noise))))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("quality")
                                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("high").executes(context -> quality(context, FftQuality.HIGH)))
                                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("low").executes(context -> quality(context, FftQuality.LOW)))
                                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("auto").executes(context -> {
                                    GpuFftProcessor.setAutomaticQuality();
                                    context.getSource().sendFeedback(Component.translatable("command.watermelonmod.veil.quality_auto"));
                                    return 1;
                                })))
        ));
    }

    /** Adds an optional trailing {@code noise} argument to a blur-parameter node. */
    private static <T extends ArgumentBuilder<FabricClientCommandSource, T>> T withNoise(T node, KernelAction action) {
        return node
                .executes(context -> action.run(context, DEFAULT_NOISE))
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, Float>argument("noise", FloatArgumentType.floatArg(0.0F, MAX_NOISE))
                        .executes(context -> action.run(context, FloatArgumentType.getFloat(context, "noise"))));
    }

    private static int apply(CommandContext<FabricClientCommandSource> context, VeilKernel.Type type, float size, float angle, float noise) {
        VeilClientState.setDegradation(new VeilKernel(type, size, angle, noise));
        context.getSource().sendFeedback(Component.translatable("command.watermelonmod.veil.on"));
        return 1;
    }

    private static int quality(CommandContext<FabricClientCommandSource> context, FftQuality quality) {
        GpuFftProcessor.setQuality(quality);
        context.getSource().sendFeedback(Component.translatable("command.watermelonmod.veil.quality", quality.width(), quality.height()));
        return 1;
    }

    @FunctionalInterface
    private interface KernelAction {
        int run(CommandContext<FabricClientCommandSource> context, float noise);
    }
}
