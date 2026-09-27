package com.jones.watermelonmod.client.veil;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.goggles.GogglesSettingsService;
import com.jones.watermelonmod.item.custom.GogglesItem;
import com.jones.watermelonmod.item.custom.VeilGogglesItem;
import com.jones.watermelonmod.veil.VeilKernel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Starts a timed, single-parameter (angle) Veil recompose trial for whoever
 * is wearing Veil Goggles. Shared by the {@code /veil trial} test command, a
 * landed Sonic Radiation hit, and the Warden's Convergence attack, so all
 * three go through the same logic.
 */
public final class VeilTrials {
    private static final float NORMAL_HIT_SIZE = 40.0F;

    private VeilTrials() {
    }

    /** @return false if the player isn't wearing Veil Goggles, in which case nothing was started. */
    public static boolean startRandomAngle(Player player, int seconds) {
        return startRandomAngle(player, seconds, NORMAL_HIT_SIZE, VeilTrialState.TriggerKind.MANUAL);
    }

    /** @return false if the player isn't wearing Veil Goggles, in which case nothing was started. */
    public static boolean startRandomAngle(Player player, int seconds, float size, VeilTrialState.TriggerKind triggerKind) {
        Optional<ItemStack> goggles = GogglesEquipment.equippedGoggles(player)
                .filter(stack -> stack.getItem() instanceof VeilGogglesItem);
        if (goggles.isEmpty()) {
            return false;
        }

        ItemStack stack = goggles.get();
        GogglesSettingsService.setParameter(stack, VeilGogglesItem.KERNEL, 1.0F);
        GogglesSettingsService.setParameter(stack, VeilGogglesItem.SIZE, size);
        GogglesSettingsService.setParameter(stack, VeilGogglesItem.MODE, 3.0F);
        GogglesSettingsService.setParameter(stack, VeilGogglesItem.ANGLE, 0.0F);
        // So scrolling still targets angle even after the trial's timer ends,
        // instead of falling back to whatever V last left selected.
        VeilTuningState.select(((GogglesItem) stack.getItem()).pipeline(), VeilGogglesItem.ANGLE);

        float targetAngle = player.getRandom().nextInt(181);
        VeilClientState.setDegradation(new VeilKernel(VeilKernel.Type.MOTION, size, targetAngle, 0.01F), VeilClientState.Source.TRIAL);
        VeilTrialState.start(targetAngle, seconds, triggerKind);
        return true;
    }
}
