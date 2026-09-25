package com.jones.watermelonmod.item.custom;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesPipeline;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterials;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Deconvolution goggles. The wearer estimates the Veil's blur (first workbench
 * page) and picks a restoration filter (second page): the inverse filter 1/H,
 * a thresholded pseudo-inverse, or the Wiener filter H / (H^2 + K).
 */
public final class VeilGogglesItem extends GogglesItem {
    public static final Identifier PIPELINE_ID = WatermelonMod.id("veil");
    public static final String KERNEL = "kernel";
    public static final String SIZE = "size";
    public static final String ANGLE = "angle";
    public static final String MODE = "mode";
    public static final String EPSILON = "epsilon";
    public static final String LOG_K = "log_k";
    public static final int MODE_OFF = 0;
    public static final GogglesPipeline PIPELINE = new GogglesPipeline(PIPELINE_ID, parameters());

    private static final String[] KERNEL_NAMES = {"gaussian", "motion", "defocus"};
    private static final String[] MODE_NAMES = {"off", "inverse", "pseudo_inverse", "wiener"};

    public VeilGogglesItem(Item.Properties properties) {
        super(ArmorMaterials.LEATHER, PIPELINE,
                properties.component(DataComponents.DYED_COLOR, new DyedItemColor(0x7A4FB5)));
    }

    private static Map<String, GogglesParameter> parameters() {
        Map<String, GogglesParameter> parameters = new LinkedHashMap<>();
        // Page 1: the wearer's estimate of the blur.
        parameters.put(KERNEL, new GogglesParameter(KERNEL, 1.0F, 0.0F, 2.0F));
        parameters.put(SIZE, new GogglesParameter(SIZE, 16.0F, 1.0F, 64.0F));
        parameters.put(ANGLE, new GogglesParameter(ANGLE, 0.0F, 0.0F, 180.0F));
        // Page 2: how to undo it.
        parameters.put(MODE, new GogglesParameter(MODE, MODE_OFF, 0.0F, 3.0F));
        parameters.put(EPSILON, new GogglesParameter(EPSILON, 0.1F, 0.01F, 0.5F));
        parameters.put(LOG_K, new GogglesParameter(LOG_K, -2.0F, -5.0F, 0.0F));
        return parameters;
    }

    /** Human-readable value for the workbench, with units instead of a percentage. */
    public static Component describe(String key, float value) {
        return switch (key) {
            case KERNEL -> Component.translatable("gui.watermelonmod.workbench.veil.kernel." + KERNEL_NAMES[Math.clamp(Math.round(value), 0, 2)]);
            case MODE -> Component.translatable("gui.watermelonmod.workbench.veil.mode." + MODE_NAMES[Math.clamp(Math.round(value), 0, 3)]);
            case SIZE -> Component.literal(String.format(Locale.ROOT, "%.1f px", value));
            case ANGLE -> Component.literal(String.format(Locale.ROOT, "%.0f°", value));
            case EPSILON -> Component.literal(String.format(Locale.ROOT, "%.2f", value));
            case LOG_K -> Component.literal(String.format(Locale.ROOT, "10^%.1f", value));
            default -> Component.literal(String.format(Locale.ROOT, "%.2f", value));
        };
    }
}
