package com.jones.watermelonmod.menu;

import com.jones.watermelonmod.block.entity.WorkbenchBlockEntity;
import com.jones.watermelonmod.goggles.GogglesParameter;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.ConvolutionGogglesItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shared workbench menu. It accepts only goggles and maps slider commands to
 * ordered pipeline parameters. Index zero retains the original 0..100 IDs;
 * later sliders use an offset so multi-control goggles remain extensible.
 */
public final class WorkbenchMenu extends AbstractContainerMenu {
    public static final int GOGGLES_SLOT = 0;
    public static final int ROTATE_EDGE_MATRIX = 101;
    public static final int INCREMENT_EDGE_CELL = 200;
    public static final int DECREMENT_EDGE_CELL = 220;
    private static final int ADDITIONAL_SLIDER_BUTTON_BASE = 1000;
    private static final int PLAYER_SLOT_START = 1;
    private static final int PLAYER_SLOT_END = 37;
    private final Container workbench;
    /** Up to six pipeline parameters; the screen shows them three per page. */
    public static final int SLIDER_COUNT = 6;
    private final DataSlot[] sliderPercents = createSliderSlots();

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
        // Centred in the wider workbench panel; the screen paints a slot well
        // beneath every one of these item positions.
        addStandardInventorySlots(inventory, 34, 142);
        for (DataSlot sliderPercent : sliderPercents) addDataSlot(sliderPercent);
        refreshSliderFromStack();
    }

    private static DataSlot[] createSliderSlots() {
        DataSlot[] slots = new DataSlot[SLIDER_COUNT];
        for (int index = 0; index < slots.length; index++) slots[index] = DataSlot.standalone();
        return slots;
    }

    public int sliderPercent() { return sliderPercent(0); }
    public int sliderPercent(int index) { return index >= 0 && index < sliderPercents.length ? sliderPercents[index].get() : 0; }
    public static int sliderButtonId(int parameterIndex, int percent) {
        return parameterIndex == 0 ? percent : ADDITIONAL_SLIDER_BUTTON_BASE * parameterIndex + percent;
    }
    public ItemStack gogglesStack() { return workbench.getItem(GOGGLES_SLOT); }
    public int[] convolutionKernel() {
        return gogglesStack().getItem() instanceof ConvolutionGogglesItem goggles
                ? goggles.kernel(GogglesSettingsService.get(gogglesStack())) : new int[9];
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == ROTATE_EDGE_MATRIX && gogglesStack().getItem() instanceof ConvolutionGogglesItem) {
            int[] kernel = convolutionKernel();
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    GogglesSettingsService.setParameter(gogglesStack(), ConvolutionGogglesItem.coefficientKey(column * 3 + (2 - row)), kernel[row * 3 + column]);
                }
            }
            refreshSliderFromStack();
            workbench.setChanged();
            broadcastChanges();
            return true;
        }
        if (gogglesStack().getItem() instanceof ConvolutionGogglesItem) {
            int index = buttonId >= INCREMENT_EDGE_CELL && buttonId < INCREMENT_EDGE_CELL + 9 ? buttonId - INCREMENT_EDGE_CELL
                    : buttonId >= DECREMENT_EDGE_CELL && buttonId < DECREMENT_EDGE_CELL + 9 ? buttonId - DECREMENT_EDGE_CELL : -1;
            if (index >= 0) {
                int direction = buttonId >= DECREMENT_EDGE_CELL ? -1 : 1;
                int value = convolutionKernel()[index];
                GogglesSettingsService.setParameter(gogglesStack(), ConvolutionGogglesItem.coefficientKey(index), value + direction);
                workbench.setChanged();
                broadcastChanges();
                return true;
            }
        }
        if (!(gogglesStack().getItem() instanceof GogglesItem goggles)) return false;
        int parameterIndex = sliderParameterIndex(buttonId);
        if (parameterIndex < 0) return false;
        GogglesParameter parameter = goggles.pipeline().parameters().values().stream().skip(parameterIndex).findFirst().orElse(null);
        if (parameter == null) return false;
        int percent = buttonId % ADDITIONAL_SLIDER_BUTTON_BASE;
        float value = parameter.minimum() + (parameter.maximum() - parameter.minimum()) * percent / 100.0F;
        GogglesSettingsService.setParameter(gogglesStack(), parameter.key(), value);
        sliderPercents[parameterIndex].set(percent);
        workbench.setChanged();
        broadcastChanges();
        return true;
    }

    private void refreshSliderFromStack() {
        if (gogglesStack().getItem() instanceof GogglesItem goggles) {
            int index = 0;
            for (GogglesParameter parameter : goggles.pipeline().parameters().values()) {
                if (index >= sliderPercents.length) break;
                float value = GogglesSettingsService.get(gogglesStack()).value(parameter.key(), parameter.defaultValue());
                sliderPercents[index++].set(Math.round(100.0F * (value - parameter.minimum()) / (parameter.maximum() - parameter.minimum())));
            }
            while (index < sliderPercents.length) sliderPercents[index++].set(0);
            return;
        }
        for (DataSlot sliderPercent : sliderPercents) sliderPercent.set(0);
    }

    private int sliderParameterIndex(int buttonId) {
        if (buttonId >= 0 && buttonId <= 100) return 0;
        if (buttonId >= ADDITIONAL_SLIDER_BUTTON_BASE) {
            int index = buttonId / ADDITIONAL_SLIDER_BUTTON_BASE;
            int percent = buttonId % ADDITIONAL_SLIDER_BUTTON_BASE;
            if (index < sliderPercents.length && percent <= 100) return index;
        }
        return -1;
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
