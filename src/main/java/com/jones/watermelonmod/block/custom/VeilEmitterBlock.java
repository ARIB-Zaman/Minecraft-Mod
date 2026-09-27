package com.jones.watermelonmod.block.custom;

import com.jones.watermelonmod.block.entity.VeilEmitterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A placeable source of ambient Veil blur (see {@link VeilEmitterBlockEntity}).
 * Purely a decoration players walk near; it has no interaction of its own.
 */
public final class VeilEmitterBlock extends BaseEntityBlock {
    public static final MapCodec<VeilEmitterBlock> CODEC = simpleCodec(VeilEmitterBlock::new);

    public VeilEmitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VeilEmitterBlockEntity(pos, state);
    }
}
