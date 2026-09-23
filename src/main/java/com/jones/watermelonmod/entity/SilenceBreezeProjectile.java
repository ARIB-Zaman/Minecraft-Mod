package com.jones.watermelonmod.entity;

import com.jones.watermelonmod.item.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Ender-pearl-style throwable that establishes a Silence Dome on impact. */
public final class SilenceBreezeProjectile extends ThrowableItemProjectile {
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(0xA65CFF, 1.0F);

    public SilenceBreezeProjectile(EntityType<SilenceBreezeProjectile> type, Level level) {
        super(type, level);
    }

    public SilenceBreezeProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.SILENCE_BREEZE_PROJECTILE, owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SILENCE_BREEZE;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (level() instanceof ServerLevel serverLevel && !isRemoved()) {
            serverLevel.addFreshEntity(SilenceDomeEntity.create(serverLevel, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z));
            serverLevel.sendParticles(PURPLE_DUST, getX(), getY(), getZ(), 32, 0.5, 0.4, 0.5, 0.08);
            discard();
        }
    }
}
