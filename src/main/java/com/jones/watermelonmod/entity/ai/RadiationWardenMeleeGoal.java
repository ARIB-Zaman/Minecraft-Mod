package com.jones.watermelonmod.entity.ai;

import com.jones.watermelonmod.attack.melee.MeleeAttackDefinition;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * A close-only melee goal. It deliberately yields once a target leaves its
 * engagement range so chase and ranged attacks can resume immediately.
 */
public final class RadiationWardenMeleeGoal extends Goal {
    private final RadiationWardenEntity boss;
    private final MeleeAttackDefinition definition;
    private @Nullable LivingEntity target;
    private long nextAttackTick;

    public RadiationWardenMeleeGoal(RadiationWardenEntity boss, MeleeAttackDefinition definition) {
        this.boss = boss;
        this.definition = definition;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = boss.getTarget();
        return isValidTarget() && boss.isAttackEnabled(definition.id()) && boss.shouldPreferMelee(target);
    }

    @Override
    public boolean canContinueToUse() {
        return isValidTarget() && boss.shouldPreferMelee(target);
    }

    @Override
    public void start() {
        nextAttackTick = boss.tickCount;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (!boss.isWithinMeleeAttackRange(target)) {
            boss.getNavigation().moveTo(target, 1.0);
            return;
        }

        boss.getNavigation().stop();
        if (boss.tickCount >= nextAttackTick && boss.level() instanceof ServerLevel level) {
            boss.swing(InteractionHand.MAIN_HAND);
            boss.doHurtTarget(level, target);
            nextAttackTick = boss.tickCount + definition.cooldownTicks();
        }
    }

    @Override
    public void stop() {
        target = null;
        boss.getNavigation().stop();
    }

    private boolean isValidTarget() {
        return !boss.isFreezeBreezeFrozen() && target != null && target.isAlive();
    }
}
