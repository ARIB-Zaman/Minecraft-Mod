package com.jones.watermelonmod.attack.sonic;

import com.jones.watermelonmod.attack.IncomingAttackResolver;
import com.jones.watermelonmod.entity.RadiationWardenEntity;
import com.jones.watermelonmod.signal.CompoundSignalGenerator;
import com.jones.watermelonmod.signal.SignalClassifier;
import com.jones.watermelonmod.signal.SignalGenerator;
import com.jones.watermelonmod.signal.SonicSignal;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** Executes visuals and damage after a Sonic Radiation charge-up completes. */
public final class SonicRadiationAttackExecutor {
    private final SignalGenerator signalGenerator;

    public SonicRadiationAttackExecutor() {
        this(new CompoundSignalGenerator());
    }

    public SonicRadiationAttackExecutor(SignalGenerator signalGenerator) {
        this.signalGenerator = signalGenerator;
    }

    public boolean isInRange(RadiationWardenEntity source, LivingEntity target, SonicRadiationAttackDefinition definition) {
        return source.closerThan(target, definition.horizontalRange(), definition.verticalRange());
    }

    private static final DustParticleOptions BENEFICIAL_TELEGRAPH = new DustParticleOptions(0xFFD700, 1.5F);
    private static final DustParticleOptions DEADLY_TELEGRAPH = new DustParticleOptions(0xFF2C2C, 1.5F);

    /**
     * Generates and classifies this beam's signal at the start of the charge —
     * not at fire time — so its telegraph (and the wearer's later choice of
     * whether to catch it) has something real to react to before it arrives.
     */
    public SonicSignal beginCharge(RadiationWardenEntity source, SonicRadiationAttackDefinition definition) {
        SonicSignal signal = signalGenerator.generate(definition.signalTemplate(), source.getRandom());
        SignalClassifier.Tier tier = SignalClassifier.classify(signal);
        boolean beneficial = tier == SignalClassifier.Tier.BENEFICIAL;

        source.setChargeTelegraph(beneficial ? RadiationWardenEntity.TELEGRAPH_BENEFICIAL : RadiationWardenEntity.TELEGRAPH_DEADLY);
        source.triggerTendrilPulse();
        source.level().broadcastEntityEvent(source, (byte) 62);
        source.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, beneficial ? 1.4F : 0.7F);
        if (source.level() instanceof ServerLevel level) {
            Vec3 chest = source.position().add(source.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, source.getYRot()));
            level.sendParticles(beneficial ? BENEFICIAL_TELEGRAPH : DEADLY_TELEGRAPH,
                    chest.x, chest.y, chest.z, 40, 0.4, 0.4, 0.4, 0.02);
        }
        return signal;
    }

    public boolean fire(ServerLevel level, RadiationWardenEntity source, LivingEntity target, SonicRadiationAttackDefinition definition, SonicSignal signal) {
        if (!target.isAlive()) {
            return false;
        }
        Vec3 origin = source.position().add(source.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, source.getYRot()));
        Vec3 delta = target.getEyePosition().subtract(origin);
        Vec3 direction = delta.normalize();
        if (direction.lengthSqr() == 0.0) {
            return false;
        }

        SonicRadiationAttack attack = new SonicRadiationAttack(
                UUID.randomUUID(), definition.id(), source.getUUID(), target.getUUID(), level.getGameTime(), signal
        );
        renderVanillaStyleBeam(level, origin, delta, direction);
        source.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        // A target may dodge the damage range during the charge, but cannot
        // cancel the already-committed beam, its sound, or its animation.
        if (isInRange(source, target, definition)
                && IncomingAttackResolver.resolve(level, target, attack, level.damageSources().sonicBoom(source), definition.damage())) {
            double vertical = definition.verticalKnockback() * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double horizontal = definition.horizontalKnockback() * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            target.push(direction.x() * horizontal, direction.y() * vertical, direction.z() * horizontal);
        }
        return true;
    }

    private static void renderVanillaStyleBeam(ServerLevel level, Vec3 origin, Vec3 delta, Vec3 direction) {
        int steps = Mth.floor(delta.length()) + 7;
        for (int index = 1; index < steps; index++) {
            Vec3 particlePos = origin.add(direction.scale(index));
            level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
