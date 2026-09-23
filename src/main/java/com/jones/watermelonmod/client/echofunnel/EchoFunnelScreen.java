package com.jones.watermelonmod.client.echofunnel;

import com.jones.watermelonmod.echofunnel.EchoFunnelCaptureStore;
import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.item.ModItems;
import com.jones.watermelonmod.signal.DiscreteFourierTransform;
import com.jones.watermelonmod.signal.SonicSignal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Read-only signal viewer. Future server-backed DSP controls can be added
 * without changing the captured, discrete signal payload it displays.
 */
@Environment(EnvType.CLIENT)
public final class EchoFunnelScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.watermelonmod.echo_funnel.title");
    private static final int PANEL_WIDTH = 356;
    private static final int PANEL_HEIGHT = 330;
    private static final int CARD_WIDTH = 106;
    private static final int CARD_HEIGHT = 92;
    private static final int CARD_GAP = 7;
    private int selectedSlot = -1;

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
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xFF555555);
        graphics.fill(left + 4, top + 4, left + PANEL_WIDTH - 4, top + PANEL_HEIGHT - 4, 0xFFC6C6C6);
        graphics.text(font, TITLE, left + 12, top + 12, 0xFF303030, false);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.capture_slots"), left + 12, top + 26, 0xFF404040, false);

        List<SonicSignal> signals = capturedSignals();
        for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
            renderSignalCard(graphics, slot, slot < signals.size() ? signals.get(slot) : null, left + 12 + slot * (CARD_WIDTH + CARD_GAP), top + 44);
        }

        SonicSignal signal = selectedSignal();
        int chartLeft = left + 12;
        int chartTop = top + 154;
        int chartWidth = PANEL_WIDTH - 24;
        int chartHeight = 72;
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.spectrum"), chartLeft, chartTop - 13, 0xFF404040, false);
        graphics.fill(chartLeft, chartTop, chartLeft + chartWidth, chartTop + chartHeight, 0xFF202020);
        graphics.fill(chartLeft + 1, chartTop + 1, chartLeft + chartWidth - 1, chartTop + chartHeight - 1, 0xFF101010);
        if (signal == null) {
            graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.no_selection"), chartLeft + 91, chartTop + 31, 0xFFBFBFBF, false);
        } else {
            renderSpectrum(graphics, signal, chartLeft + 2, chartTop + 2, chartWidth - 4, chartHeight - 4);
        }

        // Reserved workspace for the upcoming frequency selection/extraction section.
        graphics.fill(left + 12, top + 240, left + PANEL_WIDTH - 12, top + PANEL_HEIGHT - 12, 0xFF9A9A9A);
        graphics.fill(left + 14, top + 242, left + PANEL_WIDTH - 14, top + PANEL_HEIGHT - 14, 0xFFBEBEBE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int top = panelTop() + 44;
            for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
                int left = panelLeft() + 12 + slot * (CARD_WIDTH + CARD_GAP);
                if (event.x() >= left && event.x() < left + CARD_WIDTH && event.y() >= top && event.y() < top + CARD_HEIGHT) {
                    selectedSlot = slot < capturedSignals().size() && selectedSlot != slot ? slot : -1;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderSignalCard(GuiGraphicsExtractor graphics, int slot, SonicSignal signal, int left, int top) {
        int borderColor = selectedSlot == slot ? 0xFFFFD35A : 0xFF555555;
        graphics.fill(left, top, left + CARD_WIDTH, top + CARD_HEIGHT, borderColor);
        graphics.fill(left + 2, top + 2, left + CARD_WIDTH - 2, top + CARD_HEIGHT - 2, 0xFF202020);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.slot", slot + 1), left + 7, top + 7, 0xFFBFBFBF, false);
        if (signal == null) {
            graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.empty"), left + 36, top + 42, 0xFF8F8F8F, false);
            return;
        }
        renderPeriodicSignal(graphics, signal, left + 5, top + 22, CARD_WIDTH - 10, CARD_HEIGHT - 28);
    }

    private List<SonicSignal> capturedSignals() {
        if (minecraft.player == null || !minecraft.player.getMainHandItem().is(ModItems.ECHO_FUNNEL)) {
            return List.of();
        }
        return minecraft.player.getMainHandItem()
                .getOrDefault(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE, EchoFunnelCaptureStore.empty())
                .signals();
    }

    private SonicSignal selectedSignal() {
        List<SonicSignal> signals = capturedSignals();
        return selectedSlot >= 0 && selectedSlot < signals.size() ? signals.get(selectedSlot) : null;
    }

    private int panelLeft() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private void renderPeriodicSignal(GuiGraphicsExtractor graphics, SonicSignal signal, int left, int top, int graphWidth, int graphHeight) {
        double peak = signal.samples().stream().mapToDouble(Math::abs).max().orElse(1.0);
        peak = Math.max(peak, 1.0E-9);
        int centerY = top + graphHeight / 2;
        graphics.fill(left, centerY, left + graphWidth, centerY + 1, 0xFF464646);
        long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        int phaseOffset = (int) (gameTime / 2L);
        for (int index = 0; index < SonicSignal.FFT_SIZE; index++) {
            int x = left + index * (graphWidth - 1) / (SonicSignal.FFT_SIZE - 1);
            int y = centerY - (int) Math.round(signal.periodicSampleAt(index + phaseOffset) / peak * (graphHeight / 2 - 4));
            graphics.fill(x, Math.min(y, centerY), x + 1, Math.max(y, centerY) + 1, 0xFF52E6E6);
            graphics.fill(x - 1, y - 1, x + 2, y + 2, 0xFFFFFFFF);
        }
    }

    private void renderSpectrum(GuiGraphicsExtractor graphics, SonicSignal signal, int left, int top, int graphWidth, int graphHeight) {
        List<Double> magnitudes = DiscreteFourierTransform.magnitudes(signal);
        double peak = Math.max(magnitudes.stream().mapToDouble(Double::doubleValue).max().orElse(1.0), 1.0E-9);
        for (int bin = 0; bin < SonicSignal.FFT_SIZE; bin++) {
            int x = left + bin * (graphWidth - 1) / SonicSignal.FFT_SIZE;
            int nextX = left + (bin + 1) * (graphWidth - 1) / SonicSignal.FFT_SIZE;
            int barHeight = (int) Math.round(magnitudes.get(bin) / peak * (graphHeight - 2));
            graphics.fill(x, top + graphHeight - barHeight, Math.max(x + 1, nextX), top + graphHeight, 0xFFE65C52);
        }
    }
}
