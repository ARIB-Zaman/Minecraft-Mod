package com.jones.watermelonmod.client.entity;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.Identifier;

/**
 * Reuses Minecraft's Warden model and vanilla texture. Future radiation-only
 * texture and animation layers belong here, not in server-side boss logic.
 */
@Environment(EnvType.CLIENT)
public final class RadiationWardenRenderer extends MobRenderer<RadiationWardenEntity, WardenRenderState, WardenModel> {
    private static final Identifier VANILLA_WARDEN_TEXTURE = Identifier.withDefaultNamespace("textures/entity/warden/warden.png");

    public RadiationWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9F);
    }

    @Override
    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    @Override
    public void extractRenderState(RadiationWardenEntity entity, WardenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.sonicBoomAnimationState.copyFrom(entity.sonicBoomAnimationState);
    }

    @Override
    public Identifier getTextureLocation(WardenRenderState state) {
        return VANILLA_WARDEN_TEXTURE;
    }
}
