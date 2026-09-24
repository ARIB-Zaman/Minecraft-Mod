package com.jones.watermelonmod.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A stationary purple dome that suppresses Radiation Warden Sonic Radiation damage. */
public final class SilenceDomeEntity extends AreaEffectCloud {
    public static final float RADIUS = 5.0F;
    public static final int DURATION_TICKS = 200;
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(0xA65CFF, 1.25F);

    public SilenceDomeEntity(EntityType<SilenceDomeEntity> type, Level level) {
        super(type, level);
    }

    public static SilenceDomeEntity create(ServerLevel level, double x, double y, double z) {
        SilenceDomeEntity dome = new SilenceDomeEntity(ModEntities.SILENCE_DOME, level);
        dome.setPos(x, y, z);
        dome.setRadius(RADIUS);
        dome.setDuration(DURATION_TICKS);
        dome.setWaitTime(0);
        dome.setCustomParticle(PURPLE_DUST);
        return dome;
    }

    public static boolean protects(ServerLevel level, Player player) {
        return level.getEntitiesOfClass(SilenceDomeEntity.class, player.getBoundingBox().inflate(RADIUS))
                .stream().anyMatch(dome -> dome.contains(player));
    }

    private boolean contains(Player player) {
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        return dx * dx + dz * dz <= RADIUS * RADIUS && player.getY() >= getY() - 1.0 && player.getY() <= getY() + RADIUS;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && isAlive()) {
            // A few points along a hemisphere each frame make the boundary readable without a custom mesh.
            for (int point = 0; point < 3; point++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double distance = Math.sqrt(random.nextDouble()) * RADIUS;
                double height = Math.sqrt(Math.max(0.0, RADIUS * RADIUS - distance * distance));
                level().addParticle(PURPLE_DUST, getX() + Math.cos(angle) * distance, getY() + height, getZ() + Math.sin(angle) * distance, 0.0, 0.01, 0.0);
            }
        }
    }
}
