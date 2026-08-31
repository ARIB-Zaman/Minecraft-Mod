package com.jones.watermelonmod.goggles;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-item DSP settings. This component is persisted on, and synchronized with, the goggles ItemStack.
 */
public record GogglesSettings(Map<String, Float> parameters) {
    public static final Codec<GogglesSettings> CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT)
            .xmap(GogglesSettings::new, GogglesSettings::parameters);
    public static final StreamCodec<RegistryFriendlyByteBuf, GogglesSettings> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public GogglesSettings {
        parameters = Map.copyOf(parameters);
    }

    public float value(String key, float defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public GogglesSettings withValue(String key, float value) {
        Map<String, Float> updated = new LinkedHashMap<>(parameters);
        updated.put(key, value);
        return new GogglesSettings(updated);
    }
}
