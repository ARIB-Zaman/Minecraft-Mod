package com.jones.watermelonmod.client.sonar;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.item.custom.SonarGogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws a top-down radar readout in the corner of the screen from the latest sonar sweep.
 * The player is always the fixed center, always facing "up"; a ring shows range, and a small
 * legend spells out what each dot color means so the display is self-explanatory.
 */
@Environment(EnvType.CLIENT)
public final class SonarHud implements HudElement {
    private static final int RADIUS = 50;
    private static final int MARGIN = 14;
    private static final int RING_COLOR = 0x5040FF80;
    private static final int TEXT_COLOR = 0xFF40FF80;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        boolean wearingSonar = GogglesEquipment.equippedGoggles(player).map(stack -> stack.getItem() instanceof SonarGogglesItem).orElse(false);
        if (!wearingSonar) return;

        Font font = client.font;
        int centerX = graphics.guiWidth() - RADIUS - MARGIN;
        int centerY = RADIUS + MARGIN + 10;

        graphics.centeredText(font, Component.literal("SONAR"), centerX, centerY - RADIUS - 12, TEXT_COLOR);
        graphics.fill(centerX - RADIUS - 2, centerY - RADIUS - 2, centerX + RADIUS + 2, centerY + RADIUS + 2, 0x80000000);
        drawRing(graphics, centerX, centerY, RADIUS / 3.0, RING_COLOR);
        drawRing(graphics, centerX, centerY, RADIUS * 2.0 / 3.0, RING_COLOR);
        drawRing(graphics, centerX, centerY, RADIUS, TEXT_COLOR);

        List<SonarEcho> blips = SonarState.currentEchoes();
        int currentTick = SonarState.clientTick();
        Vec3 eye = player.getEyePosition();
        double yaw = player.getYRot();

        for (SonarEcho blip : blips) {
            if (currentTick < blip.revealTick()) continue;
            Vec3 delta = blip.worldPos().subtract(eye);
            double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            if (horizontalDistance > SonarSweep.RANGE) continue;
            double bearing = Math.toRadians(Math.toDegrees(Math.atan2(-delta.x, delta.z)) - yaw);
            double r = (horizontalDistance / SonarSweep.RANGE) * RADIUS;
            int px = centerX + (int) Math.round(Math.sin(bearing) * r);
            int py = centerY - (int) Math.round(Math.cos(bearing) * r);
            int size = blip.isMob() ? 3 : 2;
            graphics.fill(px - size, py - size, px + size, py + size, blip.argbColor());
        }

        // "You are here", always facing up: a bright center dot plus a small arrow pointing
        // toward the top of the circle, which is always the direction the player is looking.
        graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFFFF);
        for (int row = 0; row < 5; row++) {
            int halfWidth = row / 2;
            int y = centerY - 7 - (4 - row);
            graphics.fill(centerX - halfWidth, y, centerX + halfWidth + 1, y + 1, 0xFFFFFFFF);
        }

        float cooldown = SonarState.cooldownPercent();
        int legendY = centerY + RADIUS + 6;
        if (cooldown > 0.0F) {
            int barWidth = (int) ((RADIUS * 2) * (1.0F - cooldown));
            graphics.fill(centerX - RADIUS, legendY, centerX - RADIUS + barWidth, legendY + 2, TEXT_COLOR);
            legendY += 6;
        }

        drawLegendEntry(graphics, font, centerX - RADIUS, legendY, 0xFFB0B0B0, "wall");
        drawLegendEntry(graphics, font, centerX - RADIUS + 34, legendY, 0xFFFF4040, "hostile");
        drawLegendEntry(graphics, font, centerX + RADIUS - 34, legendY, 0xFF40C0FF, "passive");
    }

    private static void drawLegendEntry(GuiGraphicsExtractor graphics, Font font, int x, int y, int swatchColor, String label) {
        graphics.fill(x, y + 1, x + 4, y + 5, swatchColor);
        graphics.text(font, label, x + 6, y, 0xFFFFFFFF, false);
    }

    private static void drawRing(GuiGraphicsExtractor graphics, int centerX, int centerY, double radius, int color) {
        int steps = 48;
        for (int i = 0; i < steps; i++) {
            double angle = (2.0 * Math.PI * i) / steps;
            int x = centerX + (int) Math.round(Math.sin(angle) * radius);
            int y = centerY - (int) Math.round(Math.cos(angle) * radius);
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }
}
