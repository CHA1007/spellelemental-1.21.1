package com.chadate.spellelemental.cilent.render.element.icon.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.cilent.render.element.icon.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class PromotionElementRenderer extends BaseElementRenderer {
    public PromotionElementRenderer(float scale, float alpha) {
        super("promotion", scale, alpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.PROMOTION_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {

        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}