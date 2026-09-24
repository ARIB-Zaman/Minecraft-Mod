package com.jones.watermelonmod.entity.ai;

import com.jones.watermelonmod.attack.sonic.SonicRadiationAttackDefinition;
import com.jones.watermelonmod.attack.sonic.SonicRadiationAttackExecutor;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/** Charge-and-fire goal that delegates every tunable value and signal to Sonic Radiation. */
public final class SonicRadiationGoal extends Goal {
    private final RadiationWardenEntity boss;
    private final SonicRadiationAttackDefinition definition;
    private final SonicRadiationAttackExecutor executor;
    private @Nullable LivingEntity target;
    private int ticksUntilFire;
    private long nextAvailableTick;
    private boolean fired;

    public SonicRadiationGoal(RadiationWardenEntity boss, SonicRadiationAttackDefinition definition, SonicRadiationAttackExecutor executor) {
        this.boss = boss;
        this.definition = definition;
        this.executor = executor;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        return !boss.isFreezeBreezeFrozen()
                && boss.tickCount >= nextAvailableTick
                && boss.isAttackEnabled(definition.id())
                && target != null
                && !boss.shouldPreferMelee(target)
                && executor.isInRange(boss, target, definition);
    }

    @Override
    public boolean canContinueToUse() {
        // Beginning the charge commits the Warden to firing. Range and melee
        // preference decide whether it can start, never whether it may abort.
        return !boss.isFreezeBreezeFrozen() && target != null && target.isAlive() && !fired;
    }

    @Override
    public void start() {
        ticksUntilFire = definition.chargeTicks();
        fired = false;
        boss.getNavigation().stop();
        executor.beginCharge(boss);
    }

    @Override
    public void tick() {
        if (target == null) return;
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--ticksUntilFire <= 0) {
            if (boss.level() instanceof ServerLevel level) {
                executor.fire(level, boss, target, definition);
            }
            fired = true;
        }
    }

    @Override
    public void stop() {
        nextAvailableTick = boss.tickCount + (fired ? definition.cooldownTicks() : 10);
        target = null;
    }
}
