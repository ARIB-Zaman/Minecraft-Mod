package com.jones.watermelonmod.block.entity;

import com.jones.watermelonmod.block.custom.RadiationShriekerBlock;
import com.jones.watermelonmod.entity.ModEntities;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Stores the finite shriek and cooldown windows for one Radiation Shrieker. */
public final class RadiationShriekerBlockEntity extends BlockEntity {
    private static final int SHRIEK_TICKS = 90;
    private static final int COOLDOWN_TICKS = 600;
    private static final int DARKNESS_RADIUS = 40;
    private int shriekTicks;
    private int cooldownTicks;

    public RadiationShriekerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIATION_SHRIEKER, pos, state);
    }

    /** Called only on a redstone rising edge. */
    public void tryActivate(ServerLevel level) {
        if (shriekTicks > 0 || cooldownTicks > 0 || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        shriekTicks = SHRIEK_TICKS;
        cooldownTicks = COOLDOWN_TICKS;
        level.setBlock(worldPosition, getBlockState().setValue(RadiationShriekerBlock.SHRIEKING, true), 3);
        level.levelEvent(3007, worldPosition, 0); // Vanilla shriek particles and shriek sound.
        setChanged();
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, RadiationShriekerBlockEntity shrieker) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (shrieker.cooldownTicks > 0) {
            shrieker.cooldownTicks--;
        }
        if (shrieker.shriekTicks > 0 && --shrieker.shriekTicks == 0) {
            level.setBlock(pos, state.setValue(RadiationShriekerBlock.SHRIEKING, false), 3);
            shrieker.summonRadiationWarden(serverLevel);
        }
        if (shrieker.cooldownTicks > 0 || shrieker.shriekTicks > 0) {
            shrieker.setChanged();
        }
    }

    private void summonRadiationWarden(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        // Unlike the vanilla warning system, this is an explicit encounter
        // trigger: its first valid rising edge must yield exactly one boss.
        RadiationWardenEntity warden = ModEntities.RADIATION_WARDEN.create(level, EntitySpawnReason.TRIGGERED);
        if (warden == null) {
            return;
        }
        warden.snapTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        warden.beginEmergence();
        level.addFreshEntity(warden);
        Warden.applyDarknessAround(level, Vec3.atCenterOf(worldPosition), null, DARKNESS_RADIUS);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        shriekTicks = input.getIntOr("radiation_shrieker_shriek_ticks", 0);
        cooldownTicks = input.getIntOr("radiation_shrieker_cooldown_ticks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("radiation_shrieker_shriek_ticks", shriekTicks);
        output.putInt("radiation_shrieker_cooldown_ticks", cooldownTicks);
    }
}
