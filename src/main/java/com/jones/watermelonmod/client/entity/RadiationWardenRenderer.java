package com.jones.watermelonmod.client.entity;

import com.jones.watermelonmod.entity.RadiationWardenEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Function;

/**
 * Reuses Minecraft's Warden model with Radiation Warden textures. Texture
 * selection stays client-side and follows the synchronized freeze state.
 */
@Environment(EnvType.CLIENT)
public final class RadiationWardenRenderer extends MobRenderer<RadiationWardenEntity, RadiationWardenRenderState, WardenModel> {
    private static final Identifier RADIATION_WARDEN_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden.png");
    private static final Identifier FROZEN_RADIATION_WARDEN_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_freeze.png");
    private static final Identifier BIOLUMINESCENT_LAYER_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_bioluminescent_layer.png");
    private static final Identifier HEART_TEXTURE = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_heart.png");
    private static final Identifier PULSATING_SPOTS_TEXTURE_1 = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_pulsating_spots_1.png");
    private static final Identifier PULSATING_SPOTS_TEXTURE_2 = Identifier.fromNamespaceAndPath("watermelonmod", "textures/entity/radiation_warden/radiation_warden_pulsating_spots_2.png");

    public RadiationWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9F);
        WardenModel bioluminescentModel = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_BIOLUMINESCENT));
        WardenModel pulsatingSpotsModel = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_PULSATING_SPOTS));
        WardenModel tendrilsModel = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_TENDRILS));
        WardenModel heartModel = new WardenModel(context.bakeLayer(ModelLayers.WARDEN_HEART));
        addWardenOverlay(state -> BIOLUMINESCENT_LAYER_TEXTURE,
                (state, ageInTicks) -> 1.0F, bioluminescentModel);
        addWardenOverlay(state -> PULSATING_SPOTS_TEXTURE_1,
                (state, ageInTicks) -> Math.max(0.0F, Mth.cos(ageInTicks * 0.045F) * 0.25F),
                pulsatingSpotsModel);
        addWardenOverlay(state -> PULSATING_SPOTS_TEXTURE_2,
                (state, ageInTicks) -> Math.max(0.0F, Mth.cos(ageInTicks * 0.045F + (float) Math.PI) * 0.25F),
                pulsatingSpotsModel);
        addWardenOverlay(state -> RADIATION_WARDEN_TEXTURE,
                (state, ageInTicks) -> state.tendrilAnimation, tendrilsModel);
        addWardenOverlay(state -> HEART_TEXTURE,
                (state, ageInTicks) -> state.heartAnimation, heartModel);
    }

    /**
     * Vanilla's WardenModel is declared for WardenRenderState. RadiationWardenRenderState
     * extends that state, so its bridge method accepts it safely; this local raw boundary
     * lets the vanilla overlay model remain reusable without duplicating its animation code.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addWardenOverlay(Function<RadiationWardenRenderState, Identifier> textureProvider,
                                  LivingEntityEmissiveLayer.AlphaFunction<RadiationWardenRenderState> alphaFunction,
                                  WardenModel model) {
        Function<Identifier, RenderType> renderType = RenderTypes::entityTranslucentEmissive;
        addLayer((RenderLayer) new LivingEntityEmissiveLayer(this, textureProvider, alphaFunction, model,
                renderType, false));
    }

    @Override
    public RadiationWardenRenderState createRenderState() {
        return new RadiationWardenRenderState();
    }

    @Override
    public void extractRenderState(RadiationWardenEntity entity, RadiationWardenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tendrilAnimation = entity.getTendrilAnimation(partialTicks);
        state.heartAnimation = entity.getHeartAnimation(partialTicks);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.sonicBoomAnimationState.copyFrom(entity.sonicBoomAnimationState);
        state.freezeBreezeFrozen = entity.isFreezeBreezeFrozen();
    }

    @Override
    public Identifier getTextureLocation(RadiationWardenRenderState state) {
        return state.freezeBreezeFrozen ? FROZEN_RADIATION_WARDEN_TEXTURE : RADIATION_WARDEN_TEXTURE;
    }
}
