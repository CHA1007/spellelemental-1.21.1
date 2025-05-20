package com.chadate.spellelemental.render.element.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.render.element.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public class FreezeElementRenderer extends BaseElementRenderer {
    public FreezeElementRenderer(float scale, float alpha) {
        super("freeze", scale, alpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.FREEZE_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {

        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}