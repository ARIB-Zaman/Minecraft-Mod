package com.jones.watermelonmod.network;

import com.jones.watermelonmod.echofunnel.BankedFrequency;
import com.jones.watermelonmod.echofunnel.CapturedSignal;
import com.jones.watermelonmod.echofunnel.EchoFunnelBankStore;
import com.jones.watermelonmod.echofunnel.EchoFunnelCaptureStore;
import com.jones.watermelonmod.item.ModDataComponents;
import com.jones.watermelonmod.item.ModItems;
import com.jones.watermelonmod.signal.DiscreteFourierTransform;
import com.jones.watermelonmod.signal.SonicSignal;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Validates and applies Echo Funnel extraction requests on the logical server. */
public final class EchoFunnelNetworking {
    private EchoFunnelNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(BankEchoFunnelSignalPayload.TYPE, BankEchoFunnelSignalPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BankEchoFunnelSignalPayload.TYPE, (payload, context) ->
                bank(context.player(), payload));
    }

    private static void bank(ServerPlayer player, BankEchoFunnelSignalPayload payload) {
        if (payload.signalSlot() < 0 || payload.bin() < 0 || payload.bin() >= SonicSignal.FFT_SIZE) {
            return;
        }

        ItemStack funnel = player.getMainHandItem();
        if (!funnel.is(ModItems.ECHO_FUNNEL)) {
            return;
        }
        EchoFunnelCaptureStore captures = funnel.getOrDefault(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE, EchoFunnelCaptureStore.empty());
        if (payload.signalSlot() >= captures.signals().size()) {
            return;
        }

        CapturedSignal capture = captures.captureAt(payload.signalSlot());
        if (!capture.canBank(payload.bin())) {
            return;
        }
        SonicSignal signal = capture.signal();
        List<Double> magnitudes = DiscreteFourierTransform.magnitudes(signal);
        double peak = magnitudes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (peak <= 0.0) {
            return;
        }
        EchoFunnelBankStore banks = funnel.getOrDefault(ModDataComponents.ECHO_FUNNEL_BANK_STORE, EchoFunnelBankStore.empty());
        funnel.set(ModDataComponents.ECHO_FUNNEL_BANK_STORE, banks.store(new BankedFrequency(payload.bin(), magnitudes.get(payload.bin()) / peak)));
        CapturedSignal updatedCapture = capture.bank(payload.bin());
        funnel.set(ModDataComponents.ECHO_FUNNEL_CAPTURE_STORE,
                updatedCapture.isExhausted() ? captures.remove(payload.signalSlot()) : captures.replace(payload.signalSlot(), updatedCapture));
        player.sendSystemMessage(Component.translatable("message.watermelonmod.echo_funnel.banked"));
    }
}
