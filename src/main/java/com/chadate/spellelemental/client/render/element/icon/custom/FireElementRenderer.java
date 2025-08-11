package com.chadate.spellelemental.client.render.element.icon.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.client.render.element.icon.BaseElementRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

// 火元素渲染器（透明度随时间变化）
public class FireElementRenderer extends BaseElementRenderer {
    public FireElementRenderer(float scale, float baseAlpha) {
        super("fire", scale, baseAlpha);
    }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return entity.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {
        // 调用父类渲染逻辑并传递动态透明度
        super.render(entity, poseStack, bufferSource, packedLight, alpha);
    }
}