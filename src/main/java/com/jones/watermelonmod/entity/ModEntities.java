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

    public static final EntityType<SilenceBreezeProjectile> SILENCE_BREEZE_PROJECTILE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WatermelonMod.id("silence_breeze_projectile"),
            EntityType.Builder.<SilenceBreezeProjectile>of(SilenceBreezeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, WatermelonMod.id("silence_breeze_projectile")))
    );

    public static final EntityType<SilenceDomeEntity> SILENCE_DOME = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WatermelonMod.id("silence_dome"),
            EntityType.Builder.<SilenceDomeEntity>of(SilenceDomeEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, WatermelonMod.id("silence_dome")))
    );

    public static final EntityType<FreezeBreezeProjectile> FREEZE_BREEZE_PROJECTILE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WatermelonMod.id("freeze_breeze_projectile"),
            EntityType.Builder.<FreezeBreezeProjectile>of(FreezeBreezeProjectile::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, WatermelonMod.id("freeze_breeze_projectile")))
    );

    private ModEntities() {
    }

    public static void initialize() {
        // Forces class loading during common initialization.
    }
}
