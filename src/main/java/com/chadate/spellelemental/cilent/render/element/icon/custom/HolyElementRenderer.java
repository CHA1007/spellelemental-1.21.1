package com.chadate.spellelemental.cilent.render.element.icon.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.cilent.render.element.icon.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class HolyElementRenderer extends BaseElementRenderer {
    public HolyElementRenderer(float scale, float alpha) {
        super("holy", scale, alpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.HOLY_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {

        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}