package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * A compass-style dial replacing the plain "Distance: NN°" readout: a needle
 * shows the wearer's current angle guess, a warm-to-cool glow shows how close
 * it is to the hidden target, and an outer ring drains as the timer runs out.
 * Mirrors {@code SonarHud}'s dot-plotted circle technique — no textures needed.
 */
@Environment(EnvType.CLIENT)
public final class VeilTrialHud implements HudElement {
    private static final int RADIUS = 30;
    private static final int MARGIN = 50;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!VeilTrialState.isActive()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        GogglesEquipment.equippedGoggles(player)
                .filter(stack -> stack.getItem() instanceof VeilGogglesItem)
                .ifPresent(stack -> {
                    float currentAngle = GogglesSettingsService.get(stack).value(VeilGogglesItem.ANGLE, 0.0F);
                    float distance = VeilTrialState.distanceTo(currentAngle);
                    int heat = heatColor(distance);

                    int centerX = MARGIN;
                    int centerY = graphics.guiHeight() / 2;
                    Font font = client.font;

                    graphics.fill(centerX - RADIUS - 3, centerY - RADIUS - 3, centerX + RADIUS + 3, centerY + RADIUS + 3, 0x80000000);

                    // Warm/cool glow: a few concentric dot-rings in the same heat colour,
                    // brightest near the centre, standing in for a soft filled disc.
                    for (double r = RADIUS * 0.15; r < RADIUS * 0.6; r += RADIUS * 0.15) {
                        drawDotRing(graphics, centerX, centerY, r, 20, withAlpha(heat, 0x50));
                    }

                    // Outer ring drains clockwise as the timer runs out.
                    drawTimerRing(graphics, centerX, centerY, RADIUS, VeilTrialState.fractionRemaining());

                    // The needle: doubling the angle maps the blur's 180°-periodic
                    // direction onto a full 360° dial, so 0° and 180° (the same
                    // physical streak direction) point the same way.
                    drawNeedle(graphics, centerX, centerY, RADIUS - 3, currentAngle * 2.0, 0xFFFFFFFF);

                    Component label = Component.literal(Math.round(distance) + "°");
                    graphics.centeredText(font, label, centerX, centerY + RADIUS + 6, heat);
                });
    }

    private static void drawTimerRing(GuiGraphicsExtractor graphics, int centerX, int centerY, double radius, float fraction) {
        int steps = 40;
        int litSteps = Math.round(steps * fraction);
        for (int i = 0; i < steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            int x = centerX + (int) Math.round(Math.sin(angle) * radius);
            int y = centerY - (int) Math.round(Math.cos(angle) * radius);
            int color = i < litSteps ? 0xFFFFFFFF : 0x40FFFFFF;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void drawDotRing(GuiGraphicsExtractor graphics, int centerX, int centerY, double radius, int steps, int color) {
        for (int i = 0; i < steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            int x = centerX + (int) Math.round(Math.sin(angle) * radius);
            int y = centerY - (int) Math.round(Math.cos(angle) * radius);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void drawNeedle(GuiGraphicsExtractor graphics, int centerX, int centerY, double length, double angleDegrees, int color) {
        double angle = Math.toRadians(angleDegrees);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        for (double r = 0; r <= length; r += 1.0) {
            int x = centerX + (int) Math.round(sin * r);
            int y = centerY - (int) Math.round(cos * r);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    /** Green when close, through yellow, to red when far — 0..90°, the widest possible angular distance. */
    private static int heatColor(float distanceDegrees) {
        float t = Math.clamp(distanceDegrees / 90.0F, 0.0F, 1.0F);
        int r, g, b;
        if (t < 0.5F) {
            float localT = t / 0.5F;
            r = lerp(0x00, 0xFF, localT);
            g = lerp(0xFF, 0xD4, localT);
            b = 0x30;
        } else {
            float localT = (t - 0.5F) / 0.5F;
            r = 0xFF;
            g = lerp(0xD4, 0x30, localT);
            b = 0x30;
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int lerp(int from, int to, float t) {
        return from + Math.round((to - from) * t);
    }

    private static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }
}
