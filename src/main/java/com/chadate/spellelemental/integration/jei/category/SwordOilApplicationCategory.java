package com.chadate.spellelemental.integration.jei.category;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.block.entity.SwordStandBlockEntity;
import com.chadate.spellelemental.integration.jei.SpellElementalJEIPlugin;
import com.chadate.spellelemental.integration.jei.recipe.SwordOilApplicationRecipe;
import com.chadate.spellelemental.register.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;



/**
 * 剑油应用 JEI 配方类别
 * 展示如何使用剑油为剑附着元素
 */
public class SwordOilApplicationCategory implements IRecipeCategory<SwordOilApplicationRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "sword_oil_application");
    private static final ResourceLocation RIGHT_CLICK_ICON = ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "textures/gui/jei/right_click_mouce_icon.png");

    private final IDrawable icon;
    
    // 布局常量
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 85;
    
    // 3D剑座渲染区域（完全居中显示）
    private static final int BLOCK_RENDER_X = (GUI_WIDTH - 100) / 2;  // 完全居中位置
    private static final int BLOCK_RENDER_Y = GUI_HEIGHT / 2;  // 垂直居中，稍微上移
    private static final int BLOCK_RENDER_SIZE = 40; // 显示尺寸
    
    // 物品槽位置
    private static final int INPUT_SWORD_X = 8;
    private static final int INPUT_SWORD_Y = 8;
    private static final int SWORD_OIL_X = 8;
    private static final int SWORD_OIL_Y = 33;
    private static final int OUTPUT_SWORD_X = 152;
    private static final int OUTPUT_SWORD_Y = 30;

    public SwordOilApplicationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SWORD_STAND.get()));
    }
    
    @Override
    public @NotNull RecipeType<SwordOilApplicationRecipe> getRecipeType() {
        return SpellElementalJEIPlugin.SWORD_OIL_APPLICATION;
    }
    
    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.spellelemental.category.sword_oil_application");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SwordOilApplicationRecipe recipe, @NotNull IFocusGroup focuses) {
        // 输入剑 - 左上角
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SWORD_X, INPUT_SWORD_Y)
            .addItemStack(recipe.getInputSword());
        
        // 剑油 - 左下角
        builder.addSlot(RecipeIngredientRole.INPUT, SWORD_OIL_X, SWORD_OIL_Y)
            .addItemStack(recipe.getSwordOil());
        
        // 剑座通过3D渲染显示在中央
        
        // 输出剑 - 右侧居中
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_SWORD_X, OUTPUT_SWORD_Y)
            .addItemStack(recipe.getOutputSword());
    }
    
    @Override
    public void draw(@NotNull SwordOilApplicationRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 绘制3D剑座方块
        render3DBlock(guiGraphics, recipe);
        
        // 绘制元素信息文本
//        drawElementInfo(guiGraphics, recipe);
        
        // 绘制使用说明
        drawUsageInfo(guiGraphics);
        
        // 绘制右键鼠标图标
        drawRightClickIcon(guiGraphics);
    }
    
    /**
     * 渲染3D剑座方块（固定视角）
     */
    private void render3DBlock(GuiGraphics guiGraphics, SwordOilApplicationRecipe recipe) {
        try {
            // 设置渲染状态
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            
            // 移动到渲染位置
            poseStack.translate(BLOCK_RENDER_X + BLOCK_RENDER_SIZE / 2.0, BLOCK_RENDER_Y + BLOCK_RENDER_SIZE / 2.0, 100);
            
            // 应用45度斜视角度
            poseStack.mulPose(Axis.XP.rotationDegrees(-25.0f)); // X轴向上倾斜45度（俯视效果）
            poseStack.mulPose(Axis.YP.rotationDegrees(25.0f));  // Y轴旋转45度（斜视角度）
            
            // 缩放方块
            float scale = BLOCK_RENDER_SIZE;
            poseStack.scale(scale, -scale, scale);
            
            // 获取剑座方块状态
            BlockState swordStandState = ModBlocks.SWORD_STAND.get().defaultBlockState();
            
            // 渲染方块
            Minecraft minecraft = Minecraft.getInstance();
            BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            
            // 使用solid渲染类型以获得最佳效果
            blockRenderer.renderSingleBlock(
                swordStandState,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.solid()
            );
            
            bufferSource.endBatch();
            
            // 渲染剑座中的剑物品（使用方块实体渲染器）
            renderSwordWithBlockEntityRenderer(poseStack, bufferSource, recipe);
            
            poseStack.popPose();
            
        } catch (Exception e) {
            ItemStack swordStandItem = new ItemStack(ModBlocks.SWORD_STAND.get());
            guiGraphics.renderItem(swordStandItem, BLOCK_RENDER_X + 8, BLOCK_RENDER_Y + 8);
        }
    }
    

    /**
     * 使用方块实体渲染器来渲染剑座中的剑物品
     */
    private void renderSwordWithBlockEntityRenderer(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, SwordOilApplicationRecipe recipe) {
        try {
            ItemStack inputSword = recipe.getInputSword();
            if (!inputSword.isEmpty() && inputSword.getItem() instanceof SwordItem) {
                Minecraft minecraft = Minecraft.getInstance();
                
                // 创建临时的剑座方块实体，并设置到客户端世界中以获得游戏时间
                SwordStandBlockEntity tempBlockEntity =
                    new SwordStandBlockEntity(
                        BlockPos.ZERO,
                        ModBlocks.SWORD_STAND.get().defaultBlockState()
                    );
                
                // 设置方块实体的世界引用，以便获得游戏时间用于动画
                if (minecraft.level != null) {
                    tempBlockEntity.setLevel(minecraft.level);
                }
                
                // 设置剑座中的剑物品
                tempBlockEntity.setSword(inputSword);
                
                // 获取剑座的方块实体渲染器
                BlockEntityRenderer<SwordStandBlockEntity> renderer =
                    minecraft.getBlockEntityRenderDispatcher().getRenderer(tempBlockEntity);
                
                if (renderer != null) {
                    // 计算当前的partialTick以获得流畅的动画效果
                    float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
                    
                    // 使用官方的方块实体渲染器来渲染剑（包含浮动动画）
                    renderer.render(
                        tempBlockEntity,
                        partialTick, // 提供真实的partialTick用于动画
                        poseStack,
                        bufferSource,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY
                    );
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 绘制使用说明
     */
    private void drawUsageInfo(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Component usageText = Component.translatable("jei.spellelemental.usage_instruction");
        // 移动到底部居中位置
        int textWidth = minecraft.font.width(usageText);
        int x = (GUI_WIDTH - textWidth) / 2;
        guiGraphics.drawString(minecraft.font, usageText, x, GUI_HEIGHT - 12, 0x666666, false);
    }
    
    /**
     * 绘制右键鼠标图标
     */
    private void drawRightClickIcon(GuiGraphics guiGraphics) {
        // 在剑座旁边显示右键图标，提示用户交互方式
        int iconX = 35;
        int iconY = 33;
        int iconSize = 16; // 图标尺寸
        int iconX1 = 120;
        int iconY1 = 33;
        int iconSize1 = 16;
        // 绑定并渲染右键鼠标图标纹理
        RenderSystem.setShaderTexture(0, RIGHT_CLICK_ICON);
        guiGraphics.blit(RIGHT_CLICK_ICON, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        RenderSystem.setShaderTexture(0, RIGHT_CLICK_ICON);
        guiGraphics.blit(RIGHT_CLICK_ICON, iconX1, iconY1, 0, 0, iconSize1, iconSize, iconSize, iconSize);
    }
    
    /**
     * 绘制元素信息
     */
    private void drawElementInfo(GuiGraphics guiGraphics, SwordOilApplicationRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // 绘制元素类型 - 在剑座下方
        Component elementText = Component.translatable("jei.spellelemental.element_type", 
            Component.translatable("element.spellelemental." + recipe.getElementType()));
        int elementTextWidth = minecraft.font.width(elementText);
        int elementX = (GUI_WIDTH - elementTextWidth) / 2;
        guiGraphics.drawString(minecraft.font, elementText, elementX, BLOCK_RENDER_Y + BLOCK_RENDER_SIZE + 8, 0x404040, false);
        
        // 绘制元素量 - 在元素类型下方
        Component amountText = Component.translatable("jei.spellelemental.element_amount", recipe.getElementAmount());
        int amountTextWidth = minecraft.font.width(amountText);
        int amountX = (GUI_WIDTH - amountTextWidth) / 2;
        guiGraphics.drawString(minecraft.font, amountText, amountX, BLOCK_RENDER_Y + BLOCK_RENDER_SIZE + 18, 0x404040, false);
    }

    @Override
    public int getWidth() {
        return GUI_WIDTH;
    }

    @Override
    public int getHeight() {
        return GUI_HEIGHT;
    }
}
