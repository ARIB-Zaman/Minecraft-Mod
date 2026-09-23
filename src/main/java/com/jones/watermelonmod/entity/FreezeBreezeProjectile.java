package com.jones.watermelonmod.entity;

import com.jones.watermelonmod.item.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Cyan wind-charge-style projectile that briefly hard-freezes the Radiation Warden. */
public final class FreezeBreezeProjectile extends AbstractWindCharge {
    private static final int FREEZE_TICKS = 10;
    private static final DustParticleOptions CYAN_DUST = new DustParticleOptions(0x62E8FF, 1.15F);

    public FreezeBreezeProjectile(EntityType<FreezeBreezeProjectile> type, Level level) {
        super(type, level);
    }

    public FreezeBreezeProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.FREEZE_BREEZE_PROJECTILE, level, owner, owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (level() instanceof ServerLevel && hitResult.getEntity() instanceof RadiationWardenEntity warden) {
            warden.freezeByBreeze(FREEZE_TICKS);
        }
        burst(position());
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        burst(hitResult.getLocation());
    }

    @Override
    protected void explode(Vec3 position) {
        burst(position);
    }

    private void burst(Vec3 position) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(CYAN_DUST, position.x, position.y, position.z, 28, 0.35, 0.35, 0.35, 0.06);
            level().playSound(null, position.x, position.y, position.z, SoundEvents.WIND_CHARGE_BURST, getSoundSource(), 0.8F, 1.35F);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.FREEZE_BREEZE);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return CYAN_DUST;
    }
}
