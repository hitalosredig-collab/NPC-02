package com.funcionarios;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/** Desenha o funcionário com o modelo humanoide de jogador, uma das skins do mod e a armadura equipada. */
public class FuncionarioRenderer extends HumanoidMobRenderer<FuncionarioEntity, HumanoidModel<FuncionarioEntity>> {
    public FuncionarioRenderer(EntityRendererProvider.Context contexto) {
        super(contexto, new HumanoidModel<>(contexto.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(contexto.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(contexto.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                contexto.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(FuncionarioEntity entidade) {
        return new ResourceLocation(FuncionariosMod.MOD_ID,
                "textures/entity/funcionario_" + Math.floorMod(entidade.getSkin(), 3) + ".png");
    }
}
