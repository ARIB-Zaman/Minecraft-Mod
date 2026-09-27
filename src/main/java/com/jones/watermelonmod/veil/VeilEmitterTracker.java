package com.jones.watermelonmod.veil;

import com.jones.watermelonmod.block.entity.VeilEmitterBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Tracks every loaded Veil Emitter so the client can find the nearest one in
 * range each tick. Deliberately has no client-only dependency: a block entity
 * that must also load on the logical server registers here safely.
 */
public final class VeilEmitterTracker {
    private static final Set<VeilEmitterBlockEntity> LOADED = new HashSet<>();

    private VeilEmitterTracker() {
    }

    public static void register(VeilEmitterBlockEntity emitter) {
        LOADED.add(emitter);
    }

    public static void unregister(VeilEmitterBlockEntity emitter) {
        LOADED.remove(emitter);
    }

    /** The kernel of the nearest emitter whose radius contains {@code position}, if any. */
    public static Optional<VeilKernel> nearestInRange(Vec3 position) {
        VeilEmitterBlockEntity closest = null;
        double closestDistanceSqr = Double.MAX_VALUE;
        for (VeilEmitterBlockEntity emitter : LOADED) {
            if (emitter.isRemoved()) {
                continue;
            }
            double distanceSqr = position.distanceToSqr(Vec3.atCenterOf(emitter.getBlockPos()));
            double radius = emitter.radius();
            if (distanceSqr <= radius * radius && distanceSqr < closestDistanceSqr) {
                closest = emitter;
                closestDistanceSqr = distanceSqr;
            }
        }
        return Optional.ofNullable(closest).map(VeilEmitterBlockEntity::kernel);
    }
}
