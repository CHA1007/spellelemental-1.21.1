package com.chadate.spellelemental.client.render;

import com.chadate.spellelemental.block.entity.SwordStandBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.NotNull;

/**
 * 剑架方块实体渲染器
 * 负责在剑架中有剑时竖直渲染剑模型
 */
public class SwordStandRenderer implements BlockEntityRenderer<SwordStandBlockEntity> {
    
    private final ItemRenderer itemRenderer;
    
    public SwordStandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }
    
    @Override
    public void render(SwordStandBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        ItemStack sword = blockEntity.getSword();
        
        // 只有当剑架中有剑且是剑类物品时才渲染
        if (sword.isEmpty() || !(sword.getItem() instanceof SwordItem)) {
            return;
        }
        
        poseStack.pushPose();
        
        // 计算上下浮动效果
        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float floatOffset = (float) Math.sin((gameTime + partialTick) * 0.1) * 0.1f;
        
        // 移动到方块中心位置，加上浮动偏移
        poseStack.translate(0.5, 1.0 + floatOffset, 0.5);
        
        // 竖直放置剑：绕X轴旋转-90度使剑竖直向上
        poseStack.mulPose(Axis.XP.rotationDegrees(0.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(0.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(135.0f));
        
        // 获取剑的模型并渲染
        BakedModel model = itemRenderer.getModel(sword, blockEntity.getLevel(), null, 0);
        
        // 使用固定显示上下文渲染剑模型
        itemRenderer.render(sword, ItemDisplayContext.FIXED, false, poseStack, bufferSource, 
                           packedLight, OverlayTexture.NO_OVERLAY, model);
        
        poseStack.popPose();
    }
    
    @Override
    public int getViewDistance() {
        // 设置合理的渲染距离
        return 64;
    }
}
