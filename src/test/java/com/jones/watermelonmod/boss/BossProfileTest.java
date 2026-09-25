package com.jones.watermelonmod.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BossProfileTest {

    @Test
    void testInitialStateIsFirstSubphase() {
        BossState state = RadiationWardenProfile.initialState();
        assertEquals(RadiationWardenProfile.PROFILE_ID, state.profileId());
        assertEquals(RadiationWardenProfile.PHASE_ONE_ID, state.phaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_ONE_ID, state.subphaseId());
    }

    @Test
    void testSubphaseTransitionsAtHealthThresholds() {
        BossProfile profile = RadiationWardenProfile.INITIAL;

        // Full health down to > 75% -> Subphase 1 (standard)
        assertEquals(RadiationWardenProfile.SUBPHASE_ONE_ID, profile.stateAtHealthFraction(1.0).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_ONE_ID, profile.stateAtHealthFraction(0.85).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_ONE_ID, profile.stateAtHealthFraction(0.75001).subphaseId());

        // 75% down to > 50% -> Subphase 2 (tier_75 darkness pulse)
        assertEquals(RadiationWardenProfile.SUBPHASE_TWO_ID, profile.stateAtHealthFraction(0.75).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_TWO_ID, profile.stateAtHealthFraction(0.60).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_TWO_ID, profile.stateAtHealthFraction(0.50001).subphaseId());

        // 50% down to > 25% -> Subphase 3 (tier_50 darkness pulse)
        assertEquals(RadiationWardenProfile.SUBPHASE_THREE_ID, profile.stateAtHealthFraction(0.50).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_THREE_ID, profile.stateAtHealthFraction(0.35).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_THREE_ID, profile.stateAtHealthFraction(0.25001).subphaseId());

        // 25% down to 0% -> Subphase 4 (tier_25_enrage continuous darkness)
        assertEquals(RadiationWardenProfile.SUBPHASE_FOUR_ID, profile.stateAtHealthFraction(0.25).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_FOUR_ID, profile.stateAtHealthFraction(0.10).subphaseId());
        assertEquals(RadiationWardenProfile.SUBPHASE_FOUR_ID, profile.stateAtHealthFraction(0.0).subphaseId());
    }

    @Test
    void testSubphaseDarknessConfiguration() {
        BossProfile profile = RadiationWardenProfile.INITIAL;

        // Subphase 1: None
        BossSubphase subphase1 = profile.subphase(profile.stateAtHealthFraction(1.0));
        assertEquals(BossDarkness.DarknessMode.NONE, subphase1.darkness().mode());

        // Subphase 2: 12 seconds pulse (240 ticks), 30 block radius
        BossSubphase subphase2 = profile.subphase(profile.stateAtHealthFraction(0.75));
        assertEquals(BossDarkness.DarknessMode.PULSE, subphase2.darkness().mode());
        assertEquals(240, subphase2.darkness().durationTicks());
        assertEquals(30.0, subphase2.darkness().radius());

        // Subphase 3: 12 seconds pulse (240 ticks), 30 block radius
        BossSubphase subphase3 = profile.subphase(profile.stateAtHealthFraction(0.50));
        assertEquals(BossDarkness.DarknessMode.PULSE, subphase3.darkness().mode());
        assertEquals(240, subphase3.darkness().durationTicks());
        assertEquals(30.0, subphase3.darkness().radius());

        // Subphase 4: Continuous (refreshes every 40 ticks, 60 ticks duration, 30 block radius)
        BossSubphase subphase4 = profile.subphase(profile.stateAtHealthFraction(0.25));
        assertEquals(BossDarkness.DarknessMode.CONTINUOUS, subphase4.darkness().mode());
        assertEquals(40, subphase4.darkness().refreshIntervalTicks());
        assertEquals(60, subphase4.darkness().durationTicks());
        assertEquals(30.0, subphase4.darkness().radius());
    }
}
