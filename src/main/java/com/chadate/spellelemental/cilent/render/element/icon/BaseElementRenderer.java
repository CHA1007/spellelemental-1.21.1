package com.chadate.spellelemental.cilent.render.element.icon;

import com.chadate.spellelemental.SpellElemental;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class BaseElementRenderer implements ElementRenderer {
    private final ResourceLocation texture;
    private final float scale;
    private final float baseAlpha;

    public BaseElementRenderer(String elementName, float scale, float baseAlpha) {
        this.texture = ResourceLocation.fromNamespaceAndPath(
                SpellElemental.MODID,
                "textures/elements/" + elementName + "_element.png"
        );
        this.scale = scale;
        this.baseAlpha = baseAlpha;
    }

    @Override
    public ResourceLocation getTexture() { return texture; }

    @Override
    public float getScale() { return scale; }

    @Override
    public LivingEntity getEntity() {return null;}

    @Override
    public float getBaseAlpha() { return baseAlpha; }

    @Override
    public boolean shouldRender(LivingEntity entity) {
        return false;
    }

    protected int getElementValue(LivingEntity entity) {
        return 0;
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {
        var matrix = poseStack.last().pose();
        var buffer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        float[][] vertices = {
                {-1, 1, 0, 0},   // 左下
                {1, 1, 1, 0},    // 右下
                {1, -1, 1, 1},   // 右上
                {-1, -1, 0, 1}    // 左上
        };

        for (float[] vertex : vertices) {
            buffer.addVertex(matrix, vertex[0], vertex[1], 0.1f)
                    .setColor(1, 1, 1, alpha)
                    .setUv(vertex[2], vertex[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(0, 1, 0);
        }
    }
}