package com.chadate.spellelemental.client.render.element.icon.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.client.render.element.icon.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class NatureElementRenderer extends BaseElementRenderer {
    public NatureElementRenderer(float scale, float alpha) {
        super("nature", scale, alpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {

        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}