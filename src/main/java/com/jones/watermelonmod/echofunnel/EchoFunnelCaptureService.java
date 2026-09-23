package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.attack.SignalBearingAttack;
import com.jones.watermelonmod.item.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Captures one signal-bearing attack into the actively raised Echo Funnel. */
public final class EchoFunnelCaptureService {
    private EchoFunnelCaptureService() {
    }

    public static boolean tryCapture(Player player, SignalBearingAttack attack) {
        if (!EchoFunnelAbsorption.isAbsorbing(player)) {
            return false;
        }
        ItemStack funnel = player.getUseItem();
        EchoFunnelCaptureStore store = funnel.getOrDefault(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE, EchoFunnelCaptureStore.empty());
        if (!store.hasCapacity()) {
            notify(player, "message.watermelonmod.echo_funnel.full");
            return false;
        }
        funnel.set(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE, store.capture(attack.signal()));
        notify(player, "message.watermelonmod.echo_funnel.captured");
        return true;
    }

    private static void notify(Player player, String translationKey) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(translationKey), true);
        }
    }
}
