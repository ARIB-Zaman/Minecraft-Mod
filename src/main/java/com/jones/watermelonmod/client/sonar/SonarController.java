package com.jones.watermelonmod.client.sonar;

import com.jones.watermelonmod.goggles.GogglesEquipment;
import com.jones.watermelonmod.item.custom.SonarGogglesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Owns everything that happens between a ping and its last echo fading: firing the sweep,
 * animating the outgoing wave, and spawning each echo's highlight in the world — at the actual
 * hit position, at the moment its real round-trip delay elapses — rather than only on a HUD.
 */
public final class SonarController {
    private static final int WAVE_RING_POINTS = 32;
    private static final float WAVE_PARTICLE_SCALE = 0.6F;
    private static final float ECHO_PARTICLE_SCALE = 1.1F;
    private static final int ECHO_PARTICLES_PER_BURST = 6;
    private static final Random RANDOM = new Random();

    /** Tracks, within the current ping, which echoes have already spawned their world highlight. */
    private static BitSet revealed = new BitSet();

    private SonarController() {
    }

    /** Called from the ping keybind. */
    public static void ping(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null || !SonarState.readyToPing()) return;
        boolean wearingSonar = GogglesEquipment.equippedGoggles(player).map(stack -> stack.getItem() instanceof SonarGogglesItem).orElse(false);
        if (!wearingSonar) return;

        List<SonarEcho> echoes = SonarSweep.fire(level, player, SonarState.clientTick());
        SonarState.firePing(echoes);
        revealed = new BitSet(echoes.size());
        level.playLocalSound(player, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Called every client tick: advances the outgoing wave ring and reveals echoes whose delay has elapsed. */
    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) return;

        animateWave(level, player);
        revealDueEchoes(level);
    }

    private static void animateWave(ClientLevel level, LocalPlayer player) {
        int ticksSincePing = SonarState.clientTick() - SonarState.lastPingTick();
        double waveRadius = ticksSincePing * DopplerMath.WAVE_SPEED;
        if (ticksSincePing < 0 || waveRadius > SonarSweep.RANGE) return;

        Vec3 eye = player.getEyePosition();
        DustParticleOptions waveParticle = new DustParticleOptions(0x80FFFF, WAVE_PARTICLE_SCALE);
        for (int i = 0; i < WAVE_RING_POINTS; i++) {
            double angle = (2.0 * Math.PI * i) / WAVE_RING_POINTS;
            double x = eye.x + Math.sin(angle) * waveRadius;
            double z = eye.z + Math.cos(angle) * waveRadius;
            level.addParticle(waveParticle, x, eye.y, z, 0.0, 0.0, 0.0);
        }
    }

    private static void revealDueEchoes(ClientLevel level) {
        List<SonarEcho> echoes = SonarState.currentEchoes();
        int currentTick = SonarState.clientTick();
        for (int i = 0; i < echoes.size(); i++) {
            if (revealed.get(i)) continue;
            SonarEcho echo = echoes.get(i);
            if (currentTick < echo.revealTick()) continue;
            spawnEchoBurst(level, echo);
            revealed.set(i);
        }
    }

    private static void spawnEchoBurst(ClientLevel level, SonarEcho echo) {
        int rgb = echo.argbColor() & 0x00FFFFFF;
        DustParticleOptions particle = new DustParticleOptions(rgb, ECHO_PARTICLE_SCALE);
        Vec3 pos = echo.worldPos();
        for (int i = 0; i < ECHO_PARTICLES_PER_BURST; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.6;
            double offsetY = (RANDOM.nextDouble() - 0.5) * 0.6;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.6;
            level.addParticle(particle, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.0, 0.0);
        }
    }
}
