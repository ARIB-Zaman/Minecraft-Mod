package com.jones.watermelonmod.block.entity;

import com.jones.watermelonmod.veil.VeilEmitterTracker;
import com.jones.watermelonmod.veil.VeilKernel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A placeable, ambient source of Veil blur: any player within {@link #radius()}
 * sees the configured {@link VeilKernel} applied to their view. Purely a
 * client-visible effect; carries no server-side gameplay logic of its own.
 */
public final class VeilEmitterBlockEntity extends BlockEntity {
    private VeilKernel kernel = new VeilKernel(VeilKernel.Type.MOTION, 40.0F, 30.0F, 0.01F);
    private float radius = 6.0F;

    public VeilEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VEIL_EMITTER, pos, state);
    }

    public VeilKernel kernel() {
        return kernel;
    }

    public float radius() {
        return radius;
    }

    public void configure(VeilKernel kernel, float radius) {
        this.kernel = kernel;
        this.radius = radius;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            VeilEmitterTracker.register(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide()) {
            VeilEmitterTracker.unregister(this);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        VeilKernel.Type type = VeilKernel.Type.fromIndex(input.getIntOr("veil_kernel_type", VeilKernel.Type.MOTION.ordinal()));
        float size = input.getFloatOr("veil_size", 40.0F);
        float angle = input.getFloatOr("veil_angle", 30.0F);
        float noise = input.getFloatOr("veil_noise", 0.01F);
        radius = input.getFloatOr("veil_radius", 6.0F);
        kernel = new VeilKernel(type, size, angle, noise);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("veil_kernel_type", kernel.type().ordinal());
        output.putFloat("veil_size", kernel.size());
        output.putFloat("veil_angle", kernel.angleDegrees());
        output.putFloat("veil_noise", kernel.noiseSigma());
        output.putFloat("veil_radius", radius);
    }

    /** Block entity NBT does not sync to the client by default; these two overrides make it do so. */
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
