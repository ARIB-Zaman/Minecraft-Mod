package com.jones.watermelonmod.boss;

import com.jones.watermelonmod.WatermelonMod;
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
    public static final Identifier SONIC_RADIATION_ID = WatermelonMod.id("sonic_radiation");
    public static final SonicRadiationAttackDefinition SONIC_RADIATION = new SonicRadiationAttackDefinition(
            SONIC_RADIATION_ID, 34, 40, 15.0, 20.0, 10.0F, 2.5, 0.5,
            new SonicSignalTemplate(0.02, 0.08, 0.65, 1.25)
    );

    public static final BossProfile INITIAL = new BossProfile(
            PROFILE_ID,
            List.of(new BossPhase(PHASE_ONE_ID, 1.0, List.of(new BossSubphase(SUBPHASE_ONE_ID, 1.0, List.of(SONIC_RADIATION_ID)))))
    );

    private RadiationWardenProfile() {
    }

    public static BossState initialState() {
        BossPhase phase = INITIAL.initialPhase();
        return new BossState(INITIAL.id(), phase.id(), phase.subphases().getFirst().id());
    }
}
