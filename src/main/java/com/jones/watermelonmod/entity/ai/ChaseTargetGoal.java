package com.jones.watermelonmod.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/** Movement-only pursuit; it deliberately never performs an attack. */
public final class ChaseTargetGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final float stoppingDistance;
    private @Nullable LivingEntity target;
    private int repathDelay;

    public ChaseTargetGoal(PathfinderMob mob, double speedModifier, float stoppingDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.stoppingDistance = stoppingDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return isTargetOutsideStoppingDistance();
    }

    @Override
    public boolean canContinueToUse() {
        return isTargetOutsideStoppingDistance();
    }

    @Override
    public void start() {
        repathDelay = 0;
    }

    @Override
    public void stop() {
        target = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (repathDelay-- <= 0) {
            repathDelay = adjustedTickDelay(10);
            mob.getNavigation().moveTo(target, speedModifier);
        }
    }

    private boolean isTargetOutsideStoppingDistance() {
        return target != null && target.isAlive() && mob.distanceToSqr(target) > stoppingDistance * stoppingDistance;
    }
}
