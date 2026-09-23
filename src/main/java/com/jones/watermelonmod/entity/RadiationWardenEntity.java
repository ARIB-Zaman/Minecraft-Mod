package com.jones.watermelonmod.entity;

import com.jones.watermelonmod.boss.BossState;
import com.jones.watermelonmod.boss.BossCombatController;
import com.jones.watermelonmod.boss.RadiationWardenProfile;
import com.jones.watermelonmod.entity.ai.ChaseTargetGoal;
import com.jones.watermelonmod.entity.ai.SonicRadiationGoal;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * Encounter shell for the Radiation Warden. This chunk supplies target pursuit
 * and health-state evaluation only; attacks remain deliberately absent.
 */
public final class RadiationWardenEntity extends Monster {
    private BossState bossState = RadiationWardenProfile.initialState();
    private final BossCombatController combatController = new BossCombatController(RadiationWardenProfile.INITIAL);
    public final AnimationState sonicBoomAnimationState = new AnimationState();
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Mth.createInsecureUUID(random),
            getDisplayName(),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public RadiationWardenEntity(EntityType<? extends RadiationWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public BossState bossState() {
        return bossState;
    }

    public void setBossState(BossState bossState) {
        this.bossState = bossState;
    }

    public Optional<Identifier> selectAttackId() {
        return combatController.selectAttack(this);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SonicRadiationGoal(this, RadiationWardenProfile.SONIC_RADIATION, new com.jones.watermelonmod.attack.sonic.SonicRadiationAttackExecutor()));
        goalSelector.addGoal(1, new ChaseTargetGoal(this, 1.0, 3.5F));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 24.0F, 1.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        combatController.tick(this);
        super.customServerAiStep(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 62) {
            sonicBoomAnimationState.start(tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("radiation_warden_profile", bossState.profileId().toString());
        output.putString("radiation_warden_phase", bossState.phaseId().toString());
        output.putString("radiation_warden_subphase", bossState.subphaseId().toString());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        Identifier profileId = Identifier.tryParse(input.getStringOr("radiation_warden_profile", ""));
        Identifier phaseId = Identifier.tryParse(input.getStringOr("radiation_warden_phase", ""));
        Identifier subphaseId = Identifier.tryParse(input.getStringOr("radiation_warden_subphase", ""));
        if (profileId != null && phaseId != null && subphaseId != null) {
            bossState = new BossState(profileId, phaseId, subphaseId);
        }
    }
}
