package com.jones.watermelonmod.client.echofunnel;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Client-only placeholder for the future server-backed signal inventory and
 * DSP controls. Opening this screen intentionally performs no item mutation.
 */
@Environment(EnvType.CLIENT)
public final class EchoFunnelScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.watermelonmod.echo_funnel.title");

    public EchoFunnelScreen() {
        super(TITLE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int panelWidth = 220;
        int panelHeight = 136;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFFC6C6C6);
        graphics.fill(left + 2, top + 2, left + panelWidth - 2, top + panelHeight - 2, 0xFF555555);
        graphics.fill(left + 4, top + 4, left + panelWidth - 4, top + panelHeight - 4, 0xFFC6C6C6);
        graphics.text(font, TITLE, left + 12, top + 12, 0xFF303030, false);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.capture_slots"), left + 12, top + 38, 0xFF404040, false);
        for (int slot = 0; slot < 3; slot++) {
            int x = left + 28 + slot * 58;
            graphics.fill(x, top + 56, x + 38, top + 94, 0xFF555555);
            graphics.fill(x + 2, top + 58, x + 36, top + 92, 0xFF373737);
            graphics.text(font, Component.literal(Integer.toString(slot + 1)), x + 16, top + 70, 0xFFBFBFBF, false);
        }
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.not_ready"), left + 12, top + 111, 0xFF555555, false);
    }
}
