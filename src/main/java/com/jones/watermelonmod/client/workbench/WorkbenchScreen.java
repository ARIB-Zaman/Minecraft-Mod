package com.jones.watermelonmod.client.workbench;

import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
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

/**
 * A deliberately texture-free workbench UI. Its slider is driven by pipeline
 * metadata, so adding another one-parameter goggles type needs no GUI fork.
 */
@Environment(EnvType.CLIENT)
public final class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private static final int SLIDER_X = 58;
    private static final int SLIDER_Y = 57;
    private static final int SLIDER_WIDTH = 100;
    private boolean draggingSlider;
    private static final int ROTATE_X = 67;
    private static final int ROTATE_Y = 91;
    private static final int ROTATE_WIDTH = 74;
    private static final int ROTATE_HEIGHT = 15;

    public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
        inventoryLabelY = 112;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF8B8B8B);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFFC6C6C6);
        graphics.text(font, Component.translatable("container.watermelonmod.workbench"), x + 8, y + 7, 0x404040, false);
        graphics.text(font, Component.literal("Goggles"), x + 18, y + 27, 0x404040, false);
        // Explicit vanilla-style slot well: the actual item is rendered by
        // AbstractContainerScreen over this frame.
        graphics.fill(x + 22, y + 38, x + 46, y + 62, 0xFF555555);
        graphics.fill(x + 24, y + 40, x + 44, y + 60, 0xFF373737);
        graphics.fill(x + 26, y + 42, x + 42, y + 58, 0xFF8B8B8B);

        if (!(menu.gogglesStack().getItem() instanceof GogglesItem goggles)) {
            graphics.text(font, Component.translatable("gui.watermelonmod.workbench.insert_goggles"), x + 58, y + 44, 0x555555, false);
            return;
        }

        if (goggles instanceof EdgeDetectionGogglesItem) {
            extractEdgeKernel(graphics, x, y);
            return;
        }

        GogglesParameter parameter = goggles.pipeline().parameters().values().stream().findFirst().orElse(null);
        if (parameter == null) return;
        Component label = parameter.key().equals("intensity")
                ? Component.translatable("gui.watermelonmod.workbench.greyscale")
                : Component.literal(parameter.key());
        graphics.text(font, label, x + SLIDER_X, y + 37, 0x404040, false);
        int trackY = y + SLIDER_Y;
        graphics.fill(x + SLIDER_X, trackY, x + SLIDER_X + SLIDER_WIDTH, trackY + 4, 0xFF555555);
        int knobX = x + SLIDER_X + Math.round((SLIDER_WIDTH - 6) * menu.sliderPercent() / 100.0F);
        graphics.fill(knobX, trackY - 4, knobX + 6, trackY + 8, 0xFF2F75B5);
        graphics.text(font, Component.literal(menu.sliderPercent() + "%"), x + 132, y + 70, 0x404040, false);
        graphics.text(font, Component.literal(goggles.pipeline().id().getPath()), x + 58, y + 87, 0x555555, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (menu.gogglesStack().getItem() instanceof EdgeDetectionGogglesItem && isOverRotateButton(event.x(), event.y())) {
            if (menu.clickMenuButton(minecraft.player, WorkbenchMenu.ROTATE_EDGE_MATRIX)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, WorkbenchMenu.ROTATE_EDGE_MATRIX);
            }
            return true;
        }
        if (isOverSlider(event.x(), event.y()) && menu.gogglesStack().getItem() instanceof GogglesItem) {
            draggingSlider = true;
            setSlider(event.x());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingSlider) {
            setSlider(event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingSlider = false;
        return super.mouseReleased(event);
    }

    private boolean isOverSlider(double mouseX, double mouseY) {
        return mouseX >= leftPos + SLIDER_X && mouseX <= leftPos + SLIDER_X + SLIDER_WIDTH
                && mouseY >= topPos + SLIDER_Y - 7 && mouseY <= topPos + SLIDER_Y + 11;
    }

    private void setSlider(double mouseX) {
        int percent = Mth.clamp((int)Math.round((mouseX - (leftPos + SLIDER_X)) * 100.0 / SLIDER_WIDTH), 0, 100);
        if (percent != menu.sliderPercent() && menu.clickMenuButton(minecraft.player, percent)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, percent);
        }
    }

    private void extractEdgeKernel(GuiGraphicsExtractor graphics, int x, int y) {
        int rotation = Math.floorMod(Math.round(menu.sliderPercent() / 100.0F * 3.0F), 4);
        int[] kernel = EdgeDetectionGogglesItem.kernelForRotation(rotation);
        graphics.text(font, Component.translatable("gui.watermelonmod.workbench.edge_kernel"), x + 58, y + 27, 0x404040, false);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int cellX = x + 70 + column * 18;
                int cellY = y + 42 + row * 15;
                graphics.fill(cellX, cellY, cellX + 16, cellY + 13, 0xFF555555);
                graphics.fill(cellX + 1, cellY + 1, cellX + 15, cellY + 12, 0xFFB9D5EA);
                String value = Integer.toString(kernel[row * 3 + column]);
                graphics.text(font, Component.literal(value), cellX + 8 - font.width(value) / 2, cellY + 3, 0x303030, false);
            }
        }
        graphics.fill(x + ROTATE_X, y + ROTATE_Y, x + ROTATE_X + ROTATE_WIDTH, y + ROTATE_Y + ROTATE_HEIGHT, 0xFF555555);
        graphics.fill(x + ROTATE_X + 1, y + ROTATE_Y + 1, x + ROTATE_X + ROTATE_WIDTH - 1, y + ROTATE_Y + ROTATE_HEIGHT - 1, 0xFF82A9C4);
        Component rotate = Component.translatable("gui.watermelonmod.workbench.rotate_kernel");
        graphics.text(font, rotate, x + ROTATE_X + (ROTATE_WIDTH - font.width(rotate)) / 2, y + ROTATE_Y + 4, 0x202020, false);
        graphics.text(font, Component.translatable("gui.watermelonmod.workbench.orientation", rotation * 90), x + 70, y + 109, 0x555555, false);
    }

    private boolean isOverRotateButton(double mouseX, double mouseY) {
        return mouseX >= leftPos + ROTATE_X && mouseX < leftPos + ROTATE_X + ROTATE_WIDTH
                && mouseY >= topPos + ROTATE_Y && mouseY < topPos + ROTATE_Y + ROTATE_HEIGHT;
    }
}
