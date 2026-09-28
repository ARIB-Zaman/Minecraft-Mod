package com.jones.watermelonmod.boss;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.minecraft.resources.Identifier;

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
        double healthFraction = boss.getHealth() / boss.getMaxHealth();
        BossState nextState = profile.stateAtHealthFraction(healthFraction);
        if (!nextState.equals(boss.bossState())) {
            boss.setBossState(nextState);
        }
        checkConvergenceThresholds(boss, healthFraction);
    }

    /**
     * The Convergence attack is a one-time dramatic event per health
     * threshold, deliberately kept outside the normal attack-selection
     * rotation so it can't repeat or be skipped by a bad roll.
     */
    private void checkConvergenceThresholds(RadiationWardenEntity boss, double healthFraction) {
        if (healthFraction <= 0.5 && !boss.isConvergence50Fired()) {
            boss.setConvergence50Fired(true);
            boss.triggerConvergence();
        } else if (healthFraction <= 0.25 && !boss.isConvergence25Fired()) {
            boss.setConvergence25Fired(true);
            boss.triggerConvergence();
        }
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
