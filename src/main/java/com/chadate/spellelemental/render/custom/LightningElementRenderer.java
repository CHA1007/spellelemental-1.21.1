package com.chadate.spellelemental.render.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.render.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class LightningElementRenderer extends BaseElementRenderer {
    public LightningElementRenderer(float scale, float alpha) {
        super("lightning", scale, alpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {

        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}