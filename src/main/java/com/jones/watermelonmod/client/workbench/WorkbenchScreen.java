package com.jones.watermelonmod.client.workbench;

import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import com.jones.watermelonmod.item.custom.ConvolutionGogglesItem;
import com.jones.watermelonmod.item.custom.SharpeningGogglesItem;
import com.jones.watermelonmod.item.custom.FrequencyFilterGogglesItem;
import com.jones.watermelonmod.item.custom.HighPassGogglesItem;
import com.jones.watermelonmod.item.custom.BandPassGogglesItem;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import com.jones.watermelonmod.menu.WorkbenchMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * A deliberately texture-free workbench UI. Its slider is driven by pipeline
 * metadata, so adding another one-parameter goggles type needs no GUI fork.
 */
@Environment(EnvType.CLIENT)
public final class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private static final int SLIDER_X = 70;
    private static final int SLIDER_Y = 41;
    private static final int SLIDER_WIDTH = 120;
    private static final int INVENTORY_X = 34;
    private static final int INVENTORY_Y = 142;
    private int draggingSlider = -1;
    private static final int ROTATE_X = 67;
    private static final int ROTATE_Y = 91;
    private static final int ROTATE_WIDTH = 74;
    private static final int ROTATE_HEIGHT = 15;
    private static final int SLIDERS_PER_PAGE = 3;
    private static final int PAGE_X = 10;
    private static final int PAGE_Y = 68;
    private static final int PAGE_WIDTH = 48;
    private static final int PAGE_HEIGHT = 14;
    private int page;

    public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 230, 234);
        titleLabelX = 8;
        titleLabelY = 8;
        inventoryLabelX = INVENTORY_X;
        inventoryLabelY = 124;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF8B8B8B);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFFC6C6C6);
        graphics.text(font, Component.literal("Goggles"), x + 18, y + 27, 0xFF404040, false);
        // Explicit vanilla-style slot well: the actual item is rendered by
        // AbstractContainerScreen over this frame.
        graphics.fill(x + 22, y + 38, x + 46, y + 62, 0xFF555555);
        graphics.fill(x + 24, y + 40, x + 44, y + 60, 0xFF373737);
        graphics.fill(x + 26, y + 42, x + 42, y + 58, 0xFF8B8B8B);

        // Inventory is a distinct recessed panel, with a visible vanilla-like
        // well for every actual menu slot.
        graphics.fill(x + INVENTORY_X - 8, y + INVENTORY_Y - 6, x + INVENTORY_X + 170, y + INVENTORY_Y + 80, 0xFF8B8B8B);
        graphics.fill(x + INVENTORY_X - 6, y + INVENTORY_Y - 4, x + INVENTORY_X + 168, y + INVENTORY_Y + 78, 0xFFC6C6C6);
        drawInventorySlots(graphics, x, y);

        if (!(menu.gogglesStack().getItem() instanceof GogglesItem goggles)) {
            graphics.text(font, Component.translatable("gui.watermelonmod.workbench.insert_goggles"), x + 58, y + 44, 0xFF555555, false);
            return;
        }

        if (goggles instanceof ConvolutionGogglesItem convolutionGoggles) {
            extractConvolutionKernel(graphics, x, y, convolutionGoggles instanceof SharpeningGogglesItem);
            return;
        }

        List<GogglesParameter> parameters = List.copyOf(goggles.pipeline().parameters().values());
        int first = firstVisibleSlider(parameters.size());
        int shown = visibleSliderCount(parameters.size());
        for (int row = 0; row < shown; row++) {
            extractSlider(graphics, x, y, goggles, parameters.get(first + row), first + row, row);
        }
        if (pageCount(parameters.size()) > 1) {
            extractPageButton(graphics, x, y, parameters.size());
        } else if (shown < 3) {
            graphics.text(font, Component.literal(goggles.pipeline().id().getPath()), x + SLIDER_X, y + (shown > 1 ? 116 : 102), 0xFF555555, false);
        }
    }

    private int parameterCount() {
        return menu.gogglesStack().getItem() instanceof GogglesItem goggles
                ? Math.min(WorkbenchMenu.SLIDER_COUNT, goggles.pipeline().parameters().size()) : 0;
    }

    private static int pageCount(int parameterCount) {
        return Math.max(1, (Math.min(parameterCount, WorkbenchMenu.SLIDER_COUNT) + SLIDERS_PER_PAGE - 1) / SLIDERS_PER_PAGE);
    }

    private int firstVisibleSlider(int parameterCount) {
        if (page >= pageCount(parameterCount)) page = 0;
        return page * SLIDERS_PER_PAGE;
    }

    private int visibleSliderCount(int parameterCount) {
        return Math.max(0, Math.min(SLIDERS_PER_PAGE, Math.min(parameterCount, WorkbenchMenu.SLIDER_COUNT) - firstVisibleSlider(parameterCount)));
    }

    private void extractPageButton(GuiGraphicsExtractor graphics, int x, int y, int parameterCount) {
        graphics.fill(x + PAGE_X, y + PAGE_Y, x + PAGE_X + PAGE_WIDTH, y + PAGE_Y + PAGE_HEIGHT, 0xFF555555);
        graphics.fill(x + PAGE_X + 1, y + PAGE_Y + 1, x + PAGE_X + PAGE_WIDTH - 1, y + PAGE_Y + PAGE_HEIGHT - 1, 0xFF82A9C4);
        Component label = Component.translatable("gui.watermelonmod.workbench.page", page + 1, pageCount(parameterCount));
        graphics.text(font, label, x + PAGE_X + (PAGE_WIDTH - font.width(label)) / 2, y + PAGE_Y + 3, 0xFF202020, false);
    }

    private boolean isOverPageButton(double mouseX, double mouseY) {
        return pageCount(parameterCount()) > 1
                && mouseX >= leftPos + PAGE_X && mouseX < leftPos + PAGE_X + PAGE_WIDTH
                && mouseY >= topPos + PAGE_Y && mouseY < topPos + PAGE_Y + PAGE_HEIGHT;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (menu.gogglesStack().getItem() instanceof ConvolutionGogglesItem && isOverMatrix(event.x(), event.y())) {
            int cell = matrixCellAt(event.x(), event.y());
            int button = event.button() == 1 ? WorkbenchMenu.DECREMENT_EDGE_CELL + cell : WorkbenchMenu.INCREMENT_EDGE_CELL + cell;
            if (menu.clickMenuButton(minecraft.player, button)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
            }
            return true;
        }
        if (menu.gogglesStack().getItem() instanceof ConvolutionGogglesItem && isOverRotateButton(event.x(), event.y())) {
            if (menu.clickMenuButton(minecraft.player, WorkbenchMenu.ROTATE_EDGE_MATRIX)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, WorkbenchMenu.ROTATE_EDGE_MATRIX);
            }
            return true;
        }
        if (isOverPageButton(event.x(), event.y())) {
            page = (page + 1) % pageCount(parameterCount());
            return true;
        }
        int slider = sliderAt(event.x(), event.y());
        if (slider >= 0 && menu.gogglesStack().getItem() instanceof GogglesItem) {
            draggingSlider = slider;
            setSlider(event.x(), slider);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingSlider >= 0) {
            setSlider(event.x(), draggingSlider);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingSlider = -1;
        return super.mouseReleased(event);
    }

    private void extractSlider(GuiGraphicsExtractor graphics, int x, int y, GogglesItem goggles, GogglesParameter parameter, int index, int row) {
        Component label = goggles instanceof VeilGogglesItem
                ? Component.translatable("gui.watermelonmod.workbench.veil." + parameter.key())
                : goggles instanceof BandPassGogglesItem
                ? Component.translatable(index == 0 ? "gui.watermelonmod.workbench.band_low_cutoff" : index == 1 ? "gui.watermelonmod.workbench.band_high_cutoff" : "gui.watermelonmod.workbench.spectrum_opacity")
                : goggles instanceof FrequencyFilterGogglesItem
                    ? Component.translatable(index == 0 ? "gui.watermelonmod.workbench.frequency_cutoff" : "gui.watermelonmod.workbench.spectrum_opacity")
                    : goggles instanceof HighPassGogglesItem
                        ? Component.translatable(index == 0 ? "gui.watermelonmod.workbench.high_pass_cutoff" : "gui.watermelonmod.workbench.spectrum_opacity")
                        : parameter.key().equals("intensity")
                            ? Component.translatable("gui.watermelonmod.workbench.greyscale")
                            : Component.literal(parameter.key());
        int step = 29;
        int labelY = 25;
        int baseTrackY = SLIDER_Y;
        int offsetY = row * step;
        graphics.text(font, label, x + SLIDER_X, y + labelY + offsetY, 0xFF404040, false);
        int trackY = y + baseTrackY + offsetY;
        graphics.fill(x + SLIDER_X, trackY, x + SLIDER_X + SLIDER_WIDTH, trackY + 4, 0xFF555555);
        int knobX = x + SLIDER_X + Math.round((SLIDER_WIDTH - 6) * menu.sliderPercent(index) / 100.0F);
        graphics.fill(knobX, trackY - 4, knobX + 6, trackY + 8, 0xFF2F75B5);
        if (goggles instanceof VeilGogglesItem) {
            // Real units, right-aligned on the label line, so players can enter what they measured.
            float value = parameter.minimum() + (parameter.maximum() - parameter.minimum()) * menu.sliderPercent(index) / 100.0F;
            Component shown = VeilGogglesItem.describe(parameter.key(), value);
            graphics.text(font, shown, x + SLIDER_X + SLIDER_WIDTH - font.width(shown), y + labelY + offsetY, 0xFF2F4F7F, false);
        } else {
            graphics.text(font, Component.literal(menu.sliderPercent(index) + "%"), x + 198, trackY - 2, 0xFF404040, false);
        }
    }

    private int sliderAt(double mouseX, double mouseY) {
        if (mouseX < leftPos + SLIDER_X || mouseX > leftPos + SLIDER_X + SLIDER_WIDTH) return -1;
        int parameters = parameterCount();
        int first = firstVisibleSlider(parameters);
        int shown = visibleSliderCount(parameters);
        int step = 29;
        int baseTrackY = SLIDER_Y;
        for (int row = 0; row < shown; row++) {
            int trackY = topPos + baseTrackY + row * step;
            if (mouseY >= trackY - 7 && mouseY <= trackY + 11) return first + row;
        }
        return -1;
    }

    private void setSlider(double mouseX, int index) {
        int percent = Mth.clamp((int)Math.round((mouseX - (leftPos + SLIDER_X)) * 100.0 / SLIDER_WIDTH), 0, 100);
        int button = WorkbenchMenu.sliderButtonId(index, percent);
        if (percent != menu.sliderPercent(index) && menu.clickMenuButton(minecraft.player, button)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
        }
    }

    private void drawInventorySlots(GuiGraphicsExtractor graphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotWell(graphics, x + INVENTORY_X + column * 18, y + INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotWell(graphics, x + INVENTORY_X + column * 18, y + INVENTORY_Y + 58);
        }
    }

    private void drawSlotWell(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF555555);
        graphics.fill(x, y, x + 16, y + 16, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF8B8B8B);
    }

    private void extractConvolutionKernel(GuiGraphicsExtractor graphics, int x, int y, boolean sharpening) {
        int[] kernel = menu.convolutionKernel();
        graphics.text(font, Component.translatable(sharpening ? "gui.watermelonmod.workbench.sharpen_kernel" : "gui.watermelonmod.workbench.edge_kernel"), x + 58, y + 27, 0xFF404040, false);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int cellX = x + 70 + column * 18;
                int cellY = y + 42 + row * 15;
                graphics.fill(cellX, cellY, cellX + 16, cellY + 13, 0xFF555555);
                graphics.fill(cellX + 1, cellY + 1, cellX + 15, cellY + 12, 0xFFB9D5EA);
                String value = Integer.toString(kernel[row * 3 + column]);
                graphics.text(font, Component.literal(value), cellX + 8 - font.width(value) / 2, cellY + 3, 0xFF303030, false);
            }
        }
        graphics.fill(x + ROTATE_X, y + ROTATE_Y, x + ROTATE_X + ROTATE_WIDTH, y + ROTATE_Y + ROTATE_HEIGHT, 0xFF555555);
        graphics.fill(x + ROTATE_X + 1, y + ROTATE_Y + 1, x + ROTATE_X + ROTATE_WIDTH - 1, y + ROTATE_Y + ROTATE_HEIGHT - 1, 0xFF82A9C4);
        Component rotate = Component.translatable("gui.watermelonmod.workbench.rotate_kernel");
        graphics.text(font, rotate, x + ROTATE_X + (ROTATE_WIDTH - font.width(rotate)) / 2, y + ROTATE_Y + 4, 0xFF202020, false);
        graphics.text(font, Component.literal("Left-click: +1    Right-click: -1"), x + 35, y + 109, 0xFF555555, false);
    }

    private boolean isOverRotateButton(double mouseX, double mouseY) {
        return mouseX >= leftPos + ROTATE_X && mouseX < leftPos + ROTATE_X + ROTATE_WIDTH
                && mouseY >= topPos + ROTATE_Y && mouseY < topPos + ROTATE_Y + ROTATE_HEIGHT;
    }

    private boolean isOverMatrix(double mouseX, double mouseY) {
        return mouseX >= leftPos + 70 && mouseX < leftPos + 124 && mouseY >= topPos + 42 && mouseY < topPos + 87;
    }

    private int matrixCellAt(double mouseX, double mouseY) {
        int column = Math.min(2, (int)(mouseX - (leftPos + 70)) / 18);
        int row = Math.min(2, (int)(mouseY - (topPos + 42)) / 15);
        return row * 3 + column;
    }
}
