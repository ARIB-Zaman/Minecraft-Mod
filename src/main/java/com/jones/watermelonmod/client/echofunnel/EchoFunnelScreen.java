package com.jones.watermelonmod.client.echofunnel;

import com.jones.watermelonmod.echofunnel.EchoFunnelCaptureStore;
import com.jones.watermelonmod.echofunnel.EchoFunnelBankStore;
import com.jones.watermelonmod.echofunnel.CapturedSignal;
import com.jones.watermelonmod.echofunnel.RewardCatalog;
import com.jones.watermelonmod.echofunnel.RewardOffer;
import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.item.ModItems;
import com.jones.watermelonmod.network.BankEchoFunnelSignalPayload;
import com.jones.watermelonmod.network.RedeemEchoFunnelRewardPayload;
import com.jones.watermelonmod.signal.DiscreteFourierTransform;
import com.jones.watermelonmod.signal.SonicSignal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/**
 * Read-only signal viewer. Future server-backed DSP controls can be added
 * without changing the captured, discrete signal payload it displays.
 *
 * <p>Every layout number is derived from {@link #SCALE} so the whole panel
 * can be resized with one constant instead of drifting apart — rendering and
 * click hit-boxes below both read from the same derived values.</p>
 */
@Environment(EnvType.CLIENT)
public final class EchoFunnelScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.watermelonmod.echo_funnel.title");

    /** Shrink (or grow) the whole panel from here; every offset below is derived from it. */
    private static final float SCALE = 0.8F;
    private static final int PANEL_WIDTH = Math.round(356 * SCALE);
    private static final int PANEL_HEIGHT = Math.round(330 * SCALE);
    private static final int MARGIN = Math.round(12 * SCALE);

    private static final int CARD_GAP = Math.round(7 * SCALE);
    private static final int CARD_WIDTH = (PANEL_WIDTH - 2 * MARGIN - (EchoFunnelCaptureStore.CAPACITY - 1) * CARD_GAP) / EchoFunnelCaptureStore.CAPACITY;
    private static final int CARD_HEIGHT = Math.round(92 * SCALE);
    private static final int CARDS_TOP = Math.round(44 * SCALE);
    private static final int CAPTURE_LABEL_TOP = Math.round(26 * SCALE);

    private static final int CHART_TOP = Math.round(154 * SCALE);
    private static final int CHART_HEIGHT = Math.round(72 * SCALE);
    private static final int CHART_LABEL_GAP = Math.round(13 * SCALE);

    private static final int LOWER_TOP = Math.round(240 * SCALE);
    private static final int LOWER_INNER_MARGIN = Math.round(14 * SCALE);
    private static final int BANK_ROW_START = Math.round(247 * SCALE);
    private static final int BANK_ROW_HEIGHT = Math.round(16 * SCALE);
    private static final int BANK_LABEL_X = Math.round(20 * SCALE);
    private static final int BANK_BAR_LEFT = Math.round(38 * SCALE);
    private static final int BANK_BAR_RIGHT = Math.round(168 * SCALE);
    private static final int BANK_BAR_FILL_WIDTH = BANK_BAR_RIGHT - BANK_BAR_LEFT - 2;
    private static final int BANK_POINTS_LABEL_X = Math.round(129 * SCALE);

    private static final int OFFER_GRID_LEFT = Math.round(184 * SCALE);
    private static final int OFFER_GRID_TOP = Math.round(246 * SCALE);
    private static final int OFFER_COL_WIDTH = Math.round(72 * SCALE);
    private static final int OFFER_ROW_HEIGHT = Math.round(24 * SCALE);
    private static final int OFFER_WIDTH = Math.round(66 * SCALE);
    private static final int OFFER_HEIGHT = Math.round(20 * SCALE);

    private int selectedSlot = -1;
    private int selectedOffer = -1;

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
        graphics.text(font, TITLE, left + MARGIN, top + MARGIN, 0xFF303030, false);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.capture_slots"), left + MARGIN, top + CAPTURE_LABEL_TOP, 0xFF404040, false);

        List<SonicSignal> signals = capturedSignals();
        for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
            renderSignalCard(graphics, slot, slot < signals.size() ? signals.get(slot) : null, left + MARGIN + slot * (CARD_WIDTH + CARD_GAP), top + CARDS_TOP);
        }

        SonicSignal signal = selectedSignal();
        int chartLeft = left + MARGIN;
        int chartTop = top + CHART_TOP;
        int chartWidth = PANEL_WIDTH - 2 * MARGIN;
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.spectrum"), chartLeft, chartTop - CHART_LABEL_GAP, 0xFF404040, false);
        graphics.fill(chartLeft, chartTop, chartLeft + chartWidth, chartTop + CHART_HEIGHT, 0xFF202020);
        graphics.fill(chartLeft + 1, chartTop + 1, chartLeft + chartWidth - 1, chartTop + CHART_HEIGHT - 1, 0xFF101010);
        if (signal == null) {
            graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.no_selection"), chartLeft + chartWidth / 2 - 40, chartTop + CHART_HEIGHT / 2 - 4, 0xFFBFBFBF, false);
        } else {
            CapturedSignal capture = selectedCapture();
            renderSpectrum(graphics, signal, capture == null ? Set.of() : Set.copyOf(capture.bankedBins()), chartLeft + 2, chartTop + 2, chartWidth - 4, CHART_HEIGHT - 4);
            renderSpectrumTooltip(graphics, signal, mouseX, mouseY);
        }

        renderLowerPanel(graphics, left, top, mouseX, mouseY);
    }

    private void renderLowerPanel(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        graphics.fill(left + MARGIN, top + LOWER_TOP, left + PANEL_WIDTH - MARGIN, top + PANEL_HEIGHT - MARGIN, 0xFF9A9A9A);
        graphics.fill(left + LOWER_INNER_MARGIN, top + LOWER_TOP + 2, left + PANEL_WIDTH - LOWER_INNER_MARGIN, top + PANEL_HEIGHT - LOWER_INNER_MARGIN, 0xFFBEBEBE);
        EchoFunnelBankStore banks = currentBanks();
        RewardOffer selected = selectedOffer >= 0 ? RewardCatalog.OFFERS.get(selectedOffer) : null;
        for (int bank = 0; bank < EchoFunnelBankStore.BANK_COUNT; bank++) {
            int y = top + BANK_ROW_START + bank * BANK_ROW_HEIGHT;
            graphics.text(font, Component.literal("B" + (bank + 1)), left + BANK_LABEL_X, y + 2, 0xFF404040, false);
            graphics.fill(left + BANK_BAR_LEFT, y, left + BANK_BAR_RIGHT, y + 10, 0xFF555555);
            graphics.fill(left + BANK_BAR_LEFT + 1, y + 1, left + BANK_BAR_RIGHT - 1, y + 9, 0xFF242424);
            int filled = (int) Math.round(BANK_BAR_FILL_WIDTH * Math.min(1.0, banks.fill(bank)));
            graphics.fill(left + BANK_BAR_LEFT + 1, y + 1, left + BANK_BAR_LEFT + 1 + filled, y + 9, bankColor(bank));
            if (selected != null) {
                int costWidth = (int) Math.round(BANK_BAR_FILL_WIDTH * selected.bankCosts().get(bank) / (double) EchoFunnelBankStore.POINT_CAPACITY);
                graphics.fill(left + BANK_BAR_LEFT + 1, y + 1, left + BANK_BAR_LEFT + 1 + costWidth, y + 9, 0x99B84242);
            }
            graphics.text(font, Component.literal(banks.points(bank) + "/" + EchoFunnelBankStore.POINT_CAPACITY), left + BANK_POINTS_LABEL_X, y + 2, 0xFFFFFFFF, false);
        }
        for (int offer = 0; offer < RewardCatalog.OFFERS.size(); offer++) {
            int x = left + OFFER_GRID_LEFT + (offer % 2) * OFFER_COL_WIDTH;
            int y = top + OFFER_GRID_TOP + (offer / 2) * OFFER_ROW_HEIGHT;
            renderOfferSlot(graphics, offer, RewardCatalog.OFFERS.get(offer), x, y, mouseX, mouseY);
        }
    }

    private void renderOfferSlot(GuiGraphicsExtractor graphics, int offerIndex, RewardOffer offer, int x, int y, int mouseX, int mouseY) {
        int border = selectedOffer == offerIndex ? 0xFFFFD35A : 0xFF555555;
        graphics.fill(x, y, x + OFFER_WIDTH, y + OFFER_HEIGHT, border);
        graphics.fill(x + 1, y + 1, x + OFFER_WIDTH - 1, y + OFFER_HEIGHT - 1, 0xFF333333);
        graphics.fakeItem(offer.createStack(), x + 3, y + 2);
        graphics.itemDecorations(font, offer.createStack(), x + 3, y + 2);
        if (mouseX >= x && mouseX < x + OFFER_WIDTH && mouseY >= y && mouseY < y + OFFER_HEIGHT) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.watermelonmod.echo_funnel.offer_detail",
                    offer.createStack().getHoverName(), offer.bankCosts().get(0), offer.bankCosts().get(1), offer.bankCosts().get(2), offer.bankCosts().get(3)), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int top = panelTop() + CARDS_TOP;
            for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
                int left = panelLeft() + MARGIN + slot * (CARD_WIDTH + CARD_GAP);
                if (event.x() >= left && event.x() < left + CARD_WIDTH && event.y() >= top && event.y() < top + CARD_HEIGHT) {
                    selectedSlot = slot < capturedSignals().size() && selectedSlot != slot ? slot : -1;
                    return true;
                }
            }
            int bin = spectrumBinAt(event.x(), event.y());
            if (bin != -1 && selectedSlot >= 0) {
                CapturedSignal capture = selectedCapture();
                if (capture == null || !capture.canBank(bin)) {
                    return true;
                }
                ClientPlayNetworking.send(new BankEchoFunnelSignalPayload(selectedSlot, bin));
                if (capture.bankedBins().size() + 1 == CapturedSignal.MAX_BANKED_BINS) {
                    selectedSlot = -1;
                }
                return true;
            }
            int offer = offerAt(event.x(), event.y());
            if (offer != -1) {
                if (selectedOffer == offer) {
                    ClientPlayNetworking.send(new RedeemEchoFunnelRewardPayload(offer));
                } else {
                    selectedOffer = offer;
                }
                return true;
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
            graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.empty"), left + CARD_WIDTH / 2 - 12, top + CARD_HEIGHT / 2 - 4, 0xFF8F8F8F, false);
            return;
        }
        renderPeriodicSignal(graphics, signal, left + 5, top + 20, CARD_WIDTH - 10, CARD_HEIGHT - 26);
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
        CapturedSignal capture = selectedCapture();
        return capture == null ? null : capture.signal();
    }

    private CapturedSignal selectedCapture() {
        if (minecraft.player == null || !minecraft.player.getMainHandItem().is(ModItems.ECHO_FUNNEL)) {
            return null;
        }
        List<CapturedSignal> captures = minecraft.player.getMainHandItem()
                .getOrDefault(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE, EchoFunnelCaptureStore.empty())
                .captures();
        return selectedSlot >= 0 && selectedSlot < captures.size() ? captures.get(selectedSlot) : null;
    }

    private EchoFunnelBankStore currentBanks() {
        if (minecraft.player == null || !minecraft.player.getMainHandItem().is(ModItems.ECHO_FUNNEL)) {
            return EchoFunnelBankStore.empty();
        }
        return minecraft.player.getMainHandItem()
                .getOrDefault(ModDataComponents.ECHO_FUNNEL_BANK_STORE, EchoFunnelBankStore.empty());
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

    private void renderSpectrum(GuiGraphicsExtractor graphics, SonicSignal signal, Set<Integer> bankedBins, int left, int top, int graphWidth, int graphHeight) {
        List<Double> magnitudes = DiscreteFourierTransform.magnitudes(signal);
        double peak = Math.max(magnitudes.stream().mapToDouble(Double::doubleValue).max().orElse(1.0), 1.0E-9);
        for (int displayBin = 0; displayBin < SonicSignal.FFT_SIZE; displayBin++) {
            int bin = displayToBin(displayBin);
            int x = left + displayBin * (graphWidth - 1) / SonicSignal.FFT_SIZE;
            int nextX = left + (displayBin + 1) * (graphWidth - 1) / SonicSignal.FFT_SIZE;
            graphics.fill(x, top, Math.max(x + 1, nextX), top + graphHeight, mutedBandColor(EchoFunnelBankStore.bankForBin(bin)));
            int barHeight = (int) Math.round(magnitudes.get(bin) / peak * (graphHeight - 2));
            graphics.fill(x, top + graphHeight - barHeight, Math.max(x + 1, nextX), top + graphHeight, 0xFFE65C52);
            if (bankedBins.contains(bin)) {
                graphics.fill(x, top, Math.max(x + 1, nextX), top + 2, 0xFFFFD35A);
            }
        }
    }

    private void renderSpectrumTooltip(GuiGraphicsExtractor graphics, SonicSignal signal, int mouseX, int mouseY) {
        int bin = spectrumBinAt(mouseX, mouseY);
        if (bin == -1) {
            return;
        }
        List<Double> magnitudes = DiscreteFourierTransform.magnitudes(signal);
        int signedFrequency = bin <= 32 ? bin : bin - SonicSignal.FFT_SIZE;
        graphics.setTooltipForNextFrame(Component.translatable("gui.watermelonmod.echo_funnel.frequency_detail", signedFrequency,
                EchoFunnelBankStore.bankForBin(bin) + 1, String.format(java.util.Locale.ROOT, "%.2f", magnitudes.get(bin))), mouseX, mouseY);
    }

    private int spectrumBinAt(double mouseX, double mouseY) {
        int left = panelLeft() + MARGIN;
        int top = panelTop() + CHART_TOP;
        int width = PANEL_WIDTH - 2 * MARGIN;
        if (mouseX < left || mouseX >= left + width || mouseY < top || mouseY >= top + CHART_HEIGHT) {
            return -1;
        }
        int displayBin = Math.min(SonicSignal.FFT_SIZE - 1, (int) ((mouseX - left) * SonicSignal.FFT_SIZE / width));
        return displayToBin(displayBin);
    }

    private static int displayToBin(int displayBin) {
        return (displayBin + SonicSignal.FFT_SIZE / 2) % SonicSignal.FFT_SIZE;
    }

    private int offerAt(double mouseX, double mouseY) {
        for (int offer = 0; offer < RewardCatalog.OFFERS.size(); offer++) {
            int x = panelLeft() + OFFER_GRID_LEFT + (offer % 2) * OFFER_COL_WIDTH;
            int y = panelTop() + OFFER_GRID_TOP + (offer / 2) * OFFER_ROW_HEIGHT;
            if (mouseX >= x && mouseX < x + OFFER_WIDTH && mouseY >= y && mouseY < y + OFFER_HEIGHT) {
                return offer;
            }
        }
        return -1;
    }

    private static int mutedBandColor(int bank) {
        return switch (bank) {
            case 0 -> 0xFF34333A;
            case 1 -> 0xFF333941;
            case 2 -> 0xFF3B3933;
            default -> 0xFF413434;
        };
    }

    private static int bankColor(int bank) {
        return switch (bank) {
            case 0 -> 0xFF7A76A8;
            case 1 -> 0xFF5E8FA8;
            case 2 -> 0xFFA08A57;
            default -> 0xFFA86464;
        };
    }
}
