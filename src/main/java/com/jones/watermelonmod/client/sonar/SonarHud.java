package com.jones.watermelonmod.client.sonar;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.item.custom.SonarGogglesItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Draws a top-down radar readout in the corner of the screen from the latest sonar sweep. */
@Environment(EnvType.CLIENT)
public final class SonarHud implements HudElement {
    private static final int RADIUS = 50;
    private static final int MARGIN = 14;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        boolean wearingSonar = GogglesEquipment.equippedGoggles(player).map(stack -> stack.getItem() instanceof SonarGogglesItem).orElse(false);
        if (!wearingSonar) return;

        int centerX = graphics.guiWidth() - RADIUS - MARGIN;
        int centerY = RADIUS + MARGIN;
        graphics.fill(centerX - RADIUS - 2, centerY - RADIUS - 2, centerX + RADIUS + 2, centerY + RADIUS + 2, 0x80000000);
        graphics.outline(centerX - RADIUS - 2, centerY - RADIUS - 2, RADIUS * 2 + 4, RADIUS * 2 + 4, 0xFF40FF80);

        List<SonarBlip> blips = SonarState.currentBlips();
        int currentTick = SonarState.clientTick();
        Vec3 eye = player.getEyePosition();
        double yaw = player.getYRot();

        for (SonarBlip blip : blips) {
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

        float cooldown = SonarState.cooldownPercent();
        if (cooldown > 0.0F) {
            int barWidth = (int) ((RADIUS * 2) * (1.0F - cooldown));
            graphics.fill(centerX - RADIUS, centerY + RADIUS + 5, centerX - RADIUS + barWidth, centerY + RADIUS + 7, 0xFF40FF80);
        }
    }
}
