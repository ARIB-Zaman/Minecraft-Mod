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

/** Red wind-charge-style projectile that damages the Radiation Warden on a direct hit. */
public final class DamageBreezeProjectile extends AbstractWindCharge {
    public static final float WARDEN_DAMAGE = 20.0F;
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(0xFF4B4B, 1.15F);

    public DamageBreezeProjectile(EntityType<DamageBreezeProjectile> type, Level level) {
        super(type, level);
    }

    public DamageBreezeProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.DAMAGE_BREEZE_PROJECTILE, level, owner, owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (level() instanceof ServerLevel serverLevel && hitResult.getEntity() instanceof RadiationWardenEntity warden) {
            LivingEntity owner = getOwner() instanceof LivingEntity livingOwner ? livingOwner : null;
            warden.hurtServer(serverLevel, damageSources().windCharge(this, owner), WARDEN_DAMAGE);
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
            serverLevel.sendParticles(RED_DUST, position.x, position.y, position.z, 28, 0.35, 0.35, 0.35, 0.06);
            level().playSound(null, position.x, position.y, position.z, SoundEvents.WIND_CHARGE_BURST, getSoundSource(), 0.8F, 0.75F);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.DAMAGE_BREEZE);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return RED_DUST;
    }
}
