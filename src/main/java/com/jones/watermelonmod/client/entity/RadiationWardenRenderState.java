package com.jones.watermelonmod.client.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.WardenRenderState;

/** Client render-only state for Radiation Warden-specific texture layers. */
@Environment(EnvType.CLIENT)
public final class RadiationWardenRenderState extends WardenRenderState {
    public boolean freezeBreezeFrozen;
}
