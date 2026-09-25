package com.jones.watermelonmod.boss;

import com.jones.watermelonmod.WatermelonMod;
import com.jones.watermelonmod.attack.melee.MeleeAttackDefinition;
import com.jones.watermelonmod.attack.sonic.SonicRadiationAttackDefinition;
import com.jones.watermelonmod.signal.SonicSignalTemplate;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * The temporary in-code profile keeps this first vertical slice small. It will
 * be replaced by a reloadable resource without changing entity state shape.
 */
public final class RadiationWardenProfile {
    public static final Identifier PROFILE_ID = WatermelonMod.id("radiation_warden");
    public static final Identifier PHASE_ONE_ID = WatermelonMod.id("phase_one");
    public static final Identifier SUBPHASE_ONE_ID = WatermelonMod.id("phase_one/standard");
    public static final Identifier SUBPHASE_TWO_ID = WatermelonMod.id("phase_one/tier_75");
    public static final Identifier SUBPHASE_THREE_ID = WatermelonMod.id("phase_one/tier_50");
    public static final Identifier SUBPHASE_FOUR_ID = WatermelonMod.id("phase_one/tier_25_enrage");
    public static final Identifier SONIC_RADIATION_ID = WatermelonMod.id("sonic_radiation");
    public static final Identifier MELEE_ID = WatermelonMod.id("melee");
    public static final MeleeAttackDefinition MELEE = new MeleeAttackDefinition(MELEE_ID, 20, 4.0);
    public static final SonicRadiationAttackDefinition SONIC_RADIATION = new SonicRadiationAttackDefinition(
            SONIC_RADIATION_ID, 34, 40, 15.0, 20.0, 10.0F, 2.5, 0.5,
            new SonicSignalTemplate(0.02, 0.08, 0.65, 1.25)
    );

    private static final List<Identifier> DEFAULT_ATTACKS = List.of(MELEE_ID, SONIC_RADIATION_ID);

    public static final BossProfile INITIAL = new BossProfile(
            PROFILE_ID,
            List.of(new BossPhase(
                    PHASE_ONE_ID,
                    1.0,
                    List.of(
                            new BossSubphase(SUBPHASE_ONE_ID, 1.0, BossDarkness.none(), DEFAULT_ATTACKS),
                            new BossSubphase(SUBPHASE_TWO_ID, 0.75, BossDarkness.pulse(240, 30.0), DEFAULT_ATTACKS),
                            new BossSubphase(SUBPHASE_THREE_ID, 0.50, BossDarkness.pulse(240, 30.0), DEFAULT_ATTACKS),
                            new BossSubphase(SUBPHASE_FOUR_ID, 0.25, BossDarkness.continuous(40, 60, 30.0), DEFAULT_ATTACKS)
                    )
            ))
    );

    private RadiationWardenProfile() {
    }

    public static BossState initialState() {
        BossPhase phase = INITIAL.initialPhase();
        return new BossState(INITIAL.id(), phase.id(), phase.subphases().getFirst().id());
    }
}
