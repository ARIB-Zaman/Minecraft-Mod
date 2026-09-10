package com.jones.watermelonmod.menu;

import com.jones.watermelonmod.block.entity.WorkbenchBlockEntity;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.EdgeDetectionGogglesItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shared workbench menu. It accepts only goggles and maps the 0..100 slider
 * command to the selected goggles pipeline's first exposed parameter.
 */
public final class WorkbenchMenu extends AbstractContainerMenu {
    public static final int GOGGLES_SLOT = 0;
    public static final int ROTATE_EDGE_MATRIX = 101;
    private static final int PLAYER_SLOT_START = 1;
    private static final int PLAYER_SLOT_END = 37;
    private final Container workbench;
    private final DataSlot sliderPercent = DataSlot.standalone();

    public WorkbenchMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(1));
    }

    public WorkbenchMenu(int containerId, Inventory inventory, WorkbenchBlockEntity workbench) {
        this(containerId, inventory, (Container) workbench);
    }

    private WorkbenchMenu(int containerId, Inventory inventory, Container workbench) {
        super(ModMenus.WORKBENCH, containerId);
        this.workbench = workbench;
        checkContainerSize(workbench, 1);
        addSlot(new Slot(workbench, GOGGLES_SLOT, 26, 42) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof GogglesItem; }
            @Override public void setChanged() { super.setChanged(); refreshSliderFromStack(); }
        });
        addStandardInventorySlots(inventory, 8, 126);
        addDataSlot(sliderPercent);
        refreshSliderFromStack();
    }

    public int sliderPercent() { return sliderPercent.get(); }
    public ItemStack gogglesStack() { return workbench.getItem(GOGGLES_SLOT); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == ROTATE_EDGE_MATRIX && gogglesStack().getItem() instanceof EdgeDetectionGogglesItem) {
            int rotation = Math.round(GogglesSettingsService.get(gogglesStack()).value("rotation", 0.0F));
            GogglesSettingsService.setParameter(gogglesStack(), "rotation", Math.floorMod(rotation + 1, 4));
            refreshSliderFromStack();
            workbench.setChanged();
            broadcastChanges();
            return true;
        }
        if (buttonId < 0 || buttonId > 100 || !(gogglesStack().getItem() instanceof GogglesItem goggles)) return false;
        GogglesParameter parameter = goggles.pipeline().parameters().values().stream().findFirst().orElse(null);
        if (parameter == null) return false;
        float value = parameter.minimum() + (parameter.maximum() - parameter.minimum()) * buttonId / 100.0F;
        GogglesSettingsService.setParameter(gogglesStack(), parameter.key(), value);
        sliderPercent.set(buttonId);
        workbench.setChanged();
        broadcastChanges();
        return true;
    }

    private void refreshSliderFromStack() {
        if (gogglesStack().getItem() instanceof GogglesItem goggles) {
            GogglesParameter parameter = goggles.pipeline().parameters().values().stream().findFirst().orElse(null);
            if (parameter != null) {
                float value = GogglesSettingsService.get(gogglesStack()).value(parameter.key(), parameter.defaultValue());
                sliderPercent.set(Math.round(100.0F * (value - parameter.minimum()) / (parameter.maximum() - parameter.minimum())));
                return;
            }
        }
        sliderPercent.set(0);
    }

    @Override public boolean stillValid(Player player) { return workbench.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (slotIndex == GOGGLES_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof GogglesItem) {
            if (!moveItemStackTo(stack, GOGGLES_SLOT, GOGGLES_SLOT + 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
