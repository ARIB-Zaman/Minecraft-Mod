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
 * <p>The panel's size is computed in {@link #init()} from whatever screen
 * space is actually available, not a fixed guess — a high GUI Scale setting
 * or a small window both shrink the available space independently of the
 * real pixel resolution, so a hardcoded size can never reliably fit. Every
 * other offset is derived from that one computed scale, and rendering and
 * click hit-boxes both read the same derived fields.</p>
 */
@Environment(EnvType.CLIENT)
public final class EchoFunnelScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.watermelonmod.echo_funnel.title");

    /** The panel's size and every offset below at scale 1.0 — full size, if the screen has room for it. */
    private static final int BASE_PANEL_WIDTH = 356;
    private static final int BASE_PANEL_HEIGHT = 330;
    /** Clear space to always leave around the panel. */
    private static final int SCREEN_MARGIN = 16;
    /** Never shrink past this, even on a tiny screen — the panel would stop being usable below it. */
    private static final float MIN_SCALE = 0.45F;

    private int panelWidth, panelHeight, margin;
    private int cardGap, cardWidth, cardHeight, cardsTop, captureLabelTop;
    private int chartTop, chartHeight, chartLabelGap;
    private int lowerTop, lowerInnerMargin, bankRowStart, bankRowHeight, bankLabelX, bankBarLeft, bankBarRight, bankBarFillWidth, bankPointsLabelX;
    private int offerGridLeft, offerGridTop, offerColWidth, offerRowHeight, offerWidth, offerHeight;

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
    protected void init() {
        super.init();
        float scale = Math.min(1.0F, Math.min(
                (width - 2.0F * SCREEN_MARGIN) / BASE_PANEL_WIDTH,
                (height - 2.0F * SCREEN_MARGIN) / BASE_PANEL_HEIGHT));
        scale = Math.max(scale, MIN_SCALE);

        panelWidth = Math.round(BASE_PANEL_WIDTH * scale);
        panelHeight = Math.round(BASE_PANEL_HEIGHT * scale);
        margin = Math.round(12 * scale);

        cardGap = Math.round(7 * scale);
        cardWidth = (panelWidth - 2 * margin - (EchoFunnelCaptureStore.CAPACITY - 1) * cardGap) / EchoFunnelCaptureStore.CAPACITY;
        cardHeight = Math.round(92 * scale);
        cardsTop = Math.round(44 * scale);
        captureLabelTop = Math.round(26 * scale);

        chartTop = Math.round(154 * scale);
        chartHeight = Math.round(72 * scale);
        chartLabelGap = Math.round(13 * scale);

        lowerTop = Math.round(240 * scale);
        lowerInnerMargin = Math.round(14 * scale);
        bankRowStart = Math.round(247 * scale);
        bankRowHeight = Math.round(16 * scale);
        bankLabelX = Math.round(20 * scale);
        bankBarLeft = Math.round(38 * scale);
        bankBarRight = Math.round(168 * scale);
        bankBarFillWidth = bankBarRight - bankBarLeft - 2;
        bankPointsLabelX = Math.round(129 * scale);

        offerGridLeft = Math.round(184 * scale);
        offerGridTop = Math.round(246 * scale);
        offerColWidth = Math.round(72 * scale);
        offerRowHeight = Math.round(24 * scale);
        offerWidth = Math.round(66 * scale);
        offerHeight = Math.round(20 * scale);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFFC6C6C6);
        graphics.fill(left + 2, top + 2, left + panelWidth - 2, top + panelHeight - 2, 0xFF555555);
        graphics.fill(left + 4, top + 4, left + panelWidth - 4, top + panelHeight - 4, 0xFFC6C6C6);
        graphics.text(font, TITLE, left + margin, top + margin, 0xFF303030, false);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.capture_slots"), left + margin, top + captureLabelTop, 0xFF404040, false);

        List<SonicSignal> signals = capturedSignals();
        for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
            renderSignalCard(graphics, slot, slot < signals.size() ? signals.get(slot) : null, left + margin + slot * (cardWidth + cardGap), top + cardsTop);
        }

        SonicSignal signal = selectedSignal();
        int chartLeft = left + margin;
        int chartTopAbs = top + chartTop;
        int chartWidth = panelWidth - 2 * margin;
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.spectrum"), chartLeft, chartTopAbs - chartLabelGap, 0xFF404040, false);
        graphics.fill(chartLeft, chartTopAbs, chartLeft + chartWidth, chartTopAbs + chartHeight, 0xFF202020);
        graphics.fill(chartLeft + 1, chartTopAbs + 1, chartLeft + chartWidth - 1, chartTopAbs + chartHeight - 1, 0xFF101010);
        if (signal == null) {
            Component none = Component.translatable("gui.watermelonmod.echo_funnel.no_selection");
            graphics.text(font, none, chartLeft + (chartWidth - font.width(none)) / 2, chartTopAbs + chartHeight / 2 - 4, 0xFFBFBFBF, false);
        } else {
            CapturedSignal capture = selectedCapture();
            renderSpectrum(graphics, signal, capture == null ? Set.of() : Set.copyOf(capture.bankedBins()), chartLeft + 2, chartTopAbs + 2, chartWidth - 4, chartHeight - 4);
            renderSpectrumTooltip(graphics, signal, mouseX, mouseY);
        }

        renderLowerPanel(graphics, left, top, mouseX, mouseY);
    }

    private void renderLowerPanel(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        graphics.fill(left + margin, top + lowerTop, left + panelWidth - margin, top + panelHeight - margin, 0xFF9A9A9A);
        graphics.fill(left + lowerInnerMargin, top + lowerTop + 2, left + panelWidth - lowerInnerMargin, top + panelHeight - lowerInnerMargin, 0xFFBEBEBE);
        EchoFunnelBankStore banks = currentBanks();
        RewardOffer selected = selectedOffer >= 0 ? RewardCatalog.OFFERS.get(selectedOffer) : null;
        for (int bank = 0; bank < EchoFunnelBankStore.BANK_COUNT; bank++) {
            int y = top + bankRowStart + bank * bankRowHeight;
            graphics.text(font, Component.literal("B" + (bank + 1)), left + bankLabelX, y + 2, 0xFF404040, false);
            graphics.fill(left + bankBarLeft, y, left + bankBarRight, y + 10, 0xFF555555);
            graphics.fill(left + bankBarLeft + 1, y + 1, left + bankBarRight - 1, y + 9, 0xFF242424);
            int filled = (int) Math.round(bankBarFillWidth * Math.min(1.0, banks.fill(bank)));
            graphics.fill(left + bankBarLeft + 1, y + 1, left + bankBarLeft + 1 + filled, y + 9, bankColor(bank));
            if (selected != null) {
                int costWidth = (int) Math.round(bankBarFillWidth * selected.bankCosts().get(bank) / (double) EchoFunnelBankStore.POINT_CAPACITY);
                graphics.fill(left + bankBarLeft + 1, y + 1, left + bankBarLeft + 1 + costWidth, y + 9, 0x99B84242);
            }
            graphics.text(font, Component.literal(banks.points(bank) + "/" + EchoFunnelBankStore.POINT_CAPACITY), left + bankPointsLabelX, y + 2, 0xFFFFFFFF, false);
        }
        for (int offer = 0; offer < RewardCatalog.OFFERS.size(); offer++) {
            int x = left + offerGridLeft + (offer % 2) * offerColWidth;
            int y = top + offerGridTop + (offer / 2) * offerRowHeight;
            renderOfferSlot(graphics, offer, RewardCatalog.OFFERS.get(offer), x, y, mouseX, mouseY);
        }
    }

    private void renderOfferSlot(GuiGraphicsExtractor graphics, int offerIndex, RewardOffer offer, int x, int y, int mouseX, int mouseY) {
        int border = selectedOffer == offerIndex ? 0xFFFFD35A : 0xFF555555;
        graphics.fill(x, y, x + offerWidth, y + offerHeight, border);
        graphics.fill(x + 1, y + 1, x + offerWidth - 1, y + offerHeight - 1, 0xFF333333);
        graphics.fakeItem(offer.createStack(), x + 3, y + 2);
        graphics.itemDecorations(font, offer.createStack(), x + 3, y + 2);
        if (mouseX >= x && mouseX < x + offerWidth && mouseY >= y && mouseY < y + offerHeight) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.watermelonmod.echo_funnel.offer_detail",
                    offer.createStack().getHoverName(), offer.bankCosts().get(0), offer.bankCosts().get(1), offer.bankCosts().get(2), offer.bankCosts().get(3)), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int top = panelTop() + cardsTop;
            for (int slot = 0; slot < EchoFunnelCaptureStore.CAPACITY; slot++) {
                int left = panelLeft() + margin + slot * (cardWidth + cardGap);
                if (event.x() >= left && event.x() < left + cardWidth && event.y() >= top && event.y() < top + cardHeight) {
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
        graphics.fill(left, top, left + cardWidth, top + cardHeight, borderColor);
        graphics.fill(left + 2, top + 2, left + cardWidth - 2, top + cardHeight - 2, 0xFF202020);
        graphics.text(font, Component.translatable("gui.watermelonmod.echo_funnel.slot", slot + 1), left + 7, top + 7, 0xFFBFBFBF, false);
        if (signal == null) {
            Component empty = Component.translatable("gui.watermelonmod.echo_funnel.empty");
            graphics.text(font, empty, left + (cardWidth - font.width(empty)) / 2, top + cardHeight / 2 - 4, 0xFF8F8F8F, false);
            return;
        }
        renderPeriodicSignal(graphics, signal, left + 5, top + 20, cardWidth - 10, cardHeight - 26);
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
        return (width - panelWidth) / 2;
    }

    private int panelTop() {
        return (height - panelHeight) / 2;
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
        int left = panelLeft() + margin;
        int top = panelTop() + chartTop;
        int width = panelWidth - 2 * margin;
        if (mouseX < left || mouseX >= left + width || mouseY < top || mouseY >= top + chartHeight) {
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
            int x = panelLeft() + offerGridLeft + (offer % 2) * offerColWidth;
            int y = panelTop() + offerGridTop + (offer / 2) * offerRowHeight;
            if (mouseX >= x && mouseX < x + offerWidth && mouseY >= y && mouseY < y + offerHeight) {
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
