package com.jones.watermelonmod.entity;

import com.jones.watermelonmod.boss.BossState;
import com.jones.watermelonmod.boss.BossCombatController;
import com.jones.watermelonmod.boss.RadiationWardenProfile;
import com.jones.watermelonmod.entity.ai.ChaseTargetGoal;
import com.jones.watermelonmod.entity.ai.RadiationWardenMeleeGoal;
import com.jones.watermelonmod.entity.ai.SonicRadiationGoal;
import com.jones.watermelonmod.network.VeilConvergencePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Encounter shell for the Radiation Warden. This chunk supplies target pursuit
 * and health-state evaluation only; attacks remain deliberately absent.
 */
public final class RadiationWardenEntity extends Monster {
    private static final EntityDataAccessor<Integer> FREEZE_BREEZE_TICKS = SynchedEntityData.defineId(RadiationWardenEntity.class, EntityDataSerializers.INT);
    /** 0 = idle (blue heart), 1 = charging a beneficial signal (yellow), 2 = charging a deadly one (red). Purely cosmetic. */
    private static final EntityDataAccessor<Integer> CHARGE_TELEGRAPH = SynchedEntityData.defineId(RadiationWardenEntity.class, EntityDataSerializers.INT);
    public static final int TELEGRAPH_NONE = 0;
    public static final int TELEGRAPH_BENEFICIAL = 1;
    public static final int TELEGRAPH_DEADLY = 2;
    private BossState bossState = RadiationWardenProfile.initialState();
    private final BossCombatController combatController = new BossCombatController(RadiationWardenProfile.INITIAL);
    public final AnimationState sonicBoomAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState emergeAnimationState = new AnimationState();
    private int emergenceTicks;
    /** Whether the Convergence attack has already fired at each health threshold, so each only happens once per fight. */
    private boolean convergence50Fired;
    private boolean convergence25Fired;
    private static final float CONVERGENCE_DAMAGE = 8.0F;
    private static final int CONVERGENCE_SECONDS = 3;
    private static final float CONVERGENCE_SIZE = 60.0F;
    private int tendrilAnimation;
    private int tendrilAnimationO;
    private int heartAnimation;
    private int heartAnimationO;
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Mth.createInsecureUUID(random),
            getDisplayName(),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public RadiationWardenEntity(EntityType<? extends RadiationWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12000;
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

    /** Allows phase/subphase definitions to opt an attack in or out without changing AI code. */
    public boolean isAttackEnabled(Identifier attackId) {
        return combatController.isAttackEnabled(this, attackId);
    }

    /** The close range in which melee must take priority over every ranged attack. */
    public boolean shouldPreferMelee(LivingEntity target) {
        double range = RadiationWardenProfile.MELEE.engagementRange();
        return target.isAlive() && distanceToSqr(target) <= range * range;
    }

    /** Applies the short hard-stun used by Freeze Breeze. */
    public void freezeByBreeze(int ticks) {
        entityData.set(FREEZE_BREEZE_TICKS, Math.max(entityData.get(FREEZE_BREEZE_TICKS), ticks));
        getNavigation().stop();
        setDeltaMovement(0.0, 0.0, 0.0);
    }

    public boolean isFreezeBreezeFrozen() {
        return entityData.get(FREEZE_BREEZE_TICKS) > 0;
    }

    public boolean isConvergence50Fired() {
        return convergence50Fired;
    }

    public void setConvergence50Fired(boolean fired) {
        convergence50Fired = fired;
    }

    public boolean isConvergence25Fired() {
        return convergence25Fired;
    }

    public void setConvergence25Fired(boolean fired) {
        convergence25Fired = fired;
    }

    /**
     * The fight's signature moment, fired once at each health threshold: an
     * unavoidable hit paired with a heavier, tighter Veil trial. Recomposing
     * it in time earns a long stagger; missing it costs extra damage.
     */
    public void triggerConvergence() {
        if (!(level() instanceof ServerLevel serverLevel) || !(getTarget() instanceof ServerPlayer target)) {
            return;
        }
        if (SilenceDomeEntity.protects(serverLevel, target)) {
            return;
        }
        playSound(SoundEvents.WARDEN_ROAR, 6.0F, 0.8F);
        serverLevel.broadcastEntityEvent(this, (byte) 62);
        target.hurtServer(serverLevel, damageSources().sonicBoom(this), CONVERGENCE_DAMAGE);
        ServerPlayNetworking.send(target, new VeilConvergencePayload(CONVERGENCE_SECONDS, CONVERGENCE_SIZE));
    }

    public int chargeTelegraph() {
        return entityData.get(CHARGE_TELEGRAPH);
    }

    public void setChargeTelegraph(int telegraph) {
        entityData.set(CHARGE_TELEGRAPH, telegraph);
    }

    /** Starts the vanilla Warden-style emergence sequence after a shrieker summon. */
    public void beginEmergence() {
        emergenceTicks = 134;
        setPose(Pose.EMERGING);
        playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
    }

    /** Starts the Warden-style tendril flash used when the sonic attack begins charging. */
    public void triggerTendrilPulse() {
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 61);
            playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 4.0F, getVoicePitch());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FREEZE_BREEZE_TICKS, 0);
        builder.define(CHARGE_TELEGRAPH, TELEGRAPH_NONE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new RadiationWardenMeleeGoal(this, RadiationWardenProfile.MELEE));
        goalSelector.addGoal(1, new SonicRadiationGoal(this, RadiationWardenProfile.SONIC_RADIATION, new com.jones.watermelonmod.attack.sonic.SonicRadiationAttackExecutor()));
        goalSelector.addGoal(2, new ChaseTargetGoal(this, 1.0, 3.5F));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 24.0F, 1.0F));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (emergenceTicks > 0) {
            emergenceTicks--;
            getNavigation().stop();
            setDeltaMovement(0.0, 0.0, 0.0);
            if (emergenceTicks == 0) {
                setPose(Pose.STANDING);
            }
            return;
        }
        int freezeTicks = entityData.get(FREEZE_BREEZE_TICKS);
        if (freezeTicks > 0) {
            entityData.set(FREEZE_BREEZE_TICKS, freezeTicks - 1);
            getNavigation().stop();
            setDeltaMovement(0.0, 0.0, 0.0);
            return;
        }
        combatController.tick(this);
        super.customServerAiStep(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            return;
        }

        // The normal Warden's calm heartbeat is a 40-tick rhythm. Keeping this
        // client-local prevents duplicate heartbeat sounds in multiplayer.
        if (tickCount % 40 == 0) {
            heartAnimation = 10;
            if (!isSilent()) {
                level().playLocalSound(getX(), getY(), getZ(), SoundEvents.WARDEN_HEARTBEAT,
                        getSoundSource(), 5.0F, getVoicePitch(), false);
            }
        }
        tendrilAnimationO = tendrilAnimation;
        if (tendrilAnimation > 0) {
            tendrilAnimation--;
        }
        heartAnimationO = heartAnimation;
        if (heartAnimation > 0) {
            heartAnimation--;
        }
    }

    /** Client renderer hook matching the vanilla Warden tendril overlay timing. */
    public float getTendrilAnimation(float partialTicks) {
        return Mth.lerp(partialTicks, tendrilAnimationO, tendrilAnimation) / 10.0F;
    }

    /** Client renderer hook matching the vanilla Warden heart overlay timing. */
    public float getHeartAnimation(float partialTicks) {
        return Mth.lerp(partialTicks, heartAnimationO, heartAnimation) / 10.0F;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            attackAnimationState.start(tickCount);
        } else if (id == 61) {
            tendrilAnimation = 10;
        } else if (id == 62) {
            sonicBoomAnimationState.start(tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (DATA_POSE.equals(accessor) && getPose() == Pose.EMERGING) {
            emergeAnimationState.start(tickCount);
        }
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        playSound(SoundEvents.WARDEN_STEP, 10.0F, 1.0F);
    }

    /** Uses the vanilla Warden's impact sound and client attack animation event. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 10.0F, getVoicePitch());
        return super.doHurtTarget(level, target);
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
        output.putInt("freeze_breeze_ticks", entityData.get(FREEZE_BREEZE_TICKS));
        output.putInt("radiation_warden_emergence_ticks", emergenceTicks);
        output.putBoolean("radiation_warden_convergence_50_fired", convergence50Fired);
        output.putBoolean("radiation_warden_convergence_25_fired", convergence25Fired);
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
        entityData.set(FREEZE_BREEZE_TICKS, input.getIntOr("freeze_breeze_ticks", 0));
        emergenceTicks = input.getIntOr("radiation_warden_emergence_ticks", 0);
        convergence50Fired = input.getBooleanOr("radiation_warden_convergence_50_fired", false);
        convergence25Fired = input.getBooleanOr("radiation_warden_convergence_25_fired", false);
    }
}
