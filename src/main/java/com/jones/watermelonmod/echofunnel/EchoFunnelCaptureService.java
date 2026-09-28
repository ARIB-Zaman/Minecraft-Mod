package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.attack.SignalBearingAttack;
import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.signal.SignalClassifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Captures one signal-bearing attack into the actively raised Echo Funnel.
 * Catching it is no longer unconditionally safe: a beneficial (high-frequency)
 * signal rewards you, a deadly (low-frequency) one costs you something else
 * instead of damage — the "Resonance Gamble."
 */
public final class EchoFunnelCaptureService {
    private static final int EFFECT_DURATION_TICKS = 100;

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
        if (SignalClassifier.classify(attack.signal()) == SignalClassifier.Tier.BENEFICIAL) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, EFFECT_DURATION_TICKS, 0), null);
            notify(player, "message.watermelonmod.echo_funnel.captured_beneficial");
        } else {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_DURATION_TICKS, 0), null);
            notify(player, "message.watermelonmod.echo_funnel.captured_deadly");
        }
        return true;
    }

    private static void notify(Player player, String translationKey) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(translationKey), true);
        }
    }
}
