package com.jones.watermelonmod.boss;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.Optional;

/**
 * Owns phase evaluation and resolves the active subphase's attack selection.
 */
public final class BossCombatController {
    private final BossProfile profile;
    private final BossAttackSelector attackSelector;

    public BossCombatController(BossProfile profile) {
        this(profile, (subphase, random) -> subphase.attackIds().stream().findFirst());
    }

    public BossCombatController(BossProfile profile, BossAttackSelector attackSelector) {
        this.profile = profile;
        this.attackSelector = attackSelector;
    }

    public void tick(RadiationWardenEntity boss) {
        if (!boss.isAlive()) {
            return;
        }
        double healthFraction = boss.getHealth() / boss.getMaxHealth();
        BossState nextState = profile.stateAtHealthFraction(healthFraction);
        if (!nextState.equals(boss.bossState())) {
            boss.setBossState(nextState);
            onSubphaseTransition(boss, profile.subphase(nextState));
        }

        tickActiveSubphase(boss, profile.subphase(boss.bossState()));
    }

    private void onSubphaseTransition(RadiationWardenEntity boss, BossSubphase subphase) {
        if (subphase.darkness().mode() == BossDarkness.DarknessMode.PULSE) {
            applyDarkness(boss, subphase.darkness().durationTicks(), subphase.darkness().radius());
        }
    }

    private void tickActiveSubphase(RadiationWardenEntity boss, BossSubphase subphase) {
        if (subphase.darkness().mode() == BossDarkness.DarknessMode.CONTINUOUS) {
            int interval = subphase.darkness().refreshIntervalTicks();
            if (interval > 0 && boss.tickCount % interval == 0) {
                applyDarkness(boss, subphase.darkness().durationTicks(), subphase.darkness().radius());
            }
        }
    }

    private void applyDarkness(RadiationWardenEntity boss, int durationTicks, double radius) {
        if (boss.level().isClientSide() || durationTicks <= 0 || radius <= 0.0) {
            return;
        }
        ServerLevel level = (ServerLevel) boss.level();
        MobEffectInstance effect = new MobEffectInstance(MobEffects.DARKNESS, durationTicks, 0, false, false);
        MobEffectUtil.addEffectToPlayersAround(level, boss, boss.position(), radius, effect, durationTicks);
    }

    public Optional<Identifier> selectAttack(RadiationWardenEntity boss) {
        BossState state = boss.bossState();
        if (!profile.id().equals(state.profileId())) {
            return Optional.empty();
        }
        return attackSelector.select(profile.subphase(state), boss.getRandom());
    }

    /** Whether a named attack belongs to the boss's currently active subphase. */
    public boolean isAttackEnabled(RadiationWardenEntity boss, Identifier attackId) {
        BossState state = boss.bossState();
        return profile.id().equals(state.profileId()) && profile.subphase(state).attackIds().contains(attackId);
    }
}
