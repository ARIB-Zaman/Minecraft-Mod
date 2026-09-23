package com.jones.watermelonmod.client.entity;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Reuses Minecraft's Warden model with Radiation Warden textures. Texture
 * selection stays client-side and follows the synchronized freeze state.
 */
@Environment(EnvType.CLIENT)
public final class RadiationWardenRenderer extends MobRenderer<RadiationWardenEntity, RadiationWardenRenderState, WardenModel> {
    private static final Identifier RADIATION_WARDEN_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden.png");
    private static final Identifier FROZEN_RADIATION_WARDEN_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_freeze.png");

    public RadiationWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9F);
    }

    @Override
    public RadiationWardenRenderState createRenderState() {
        return new RadiationWardenRenderState();
    }

    @Override
    public void extractRenderState(RadiationWardenEntity entity, RadiationWardenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.sonicBoomAnimationState.copyFrom(entity.sonicBoomAnimationState);
        state.freezeBreezeFrozen = entity.isFreezeBreezeFrozen();
    }

    @Override
    public Identifier getTextureLocation(RadiationWardenRenderState state) {
        return state.freezeBreezeFrozen ? FROZEN_RADIATION_WARDEN_TEXTURE : RADIATION_WARDEN_TEXTURE;
    }
}
