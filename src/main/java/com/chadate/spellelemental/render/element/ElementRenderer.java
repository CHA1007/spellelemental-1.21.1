package com.chadate.spellelemental.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface ElementRenderer {
    ResourceLocation getTexture();
    float getScale();
    float getBaseAlpha(); // 基础透明度（最大值）
    boolean shouldRender(LivingEntity entity);
    void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha);

    LivingEntity getEntity();
}
