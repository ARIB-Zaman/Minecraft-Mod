package com.jones.watermelonmod.entity;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Central registration point for all mod entity types. */
public final class ModEntities {
    public static final EntityType<RadiationWardenEntity> RADIATION_WARDEN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WatermelonMod.id("radiation_warden"),
            EntityType.Builder.of(RadiationWardenEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.9F)
                    .passengerAttachments(3.15F)
                    .attach(EntityAttachment.WARDEN_CHEST, 0.0F, 1.6F, 0.0F)
                    .clientTrackingRange(16)
                    .fireImmune()
                    .notInPeaceful()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, WatermelonMod.id("radiation_warden")))
    );

    private ModEntities() {
    }

    public static void initialize() {
        // Forces class loading during common initialization.
    }
}
