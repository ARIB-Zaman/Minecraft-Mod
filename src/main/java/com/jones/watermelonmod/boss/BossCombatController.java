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
