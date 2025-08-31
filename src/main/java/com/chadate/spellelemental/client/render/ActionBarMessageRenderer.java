package com.chadate.spellelemental.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 在物品栏上方渲染动作栏消息
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = "spellelemental")
public class ActionBarMessageRenderer {
    
    private static Component currentMessage = null;
    private static long messageStartTime = 0;
    private static final long MESSAGE_DURATION = 3000; // 3秒显示时间
    
    /**
     * 设置要显示的动作栏消息
     */
    public static void setMessage(Component message) {
        currentMessage = message;
        messageStartTime = System.currentTimeMillis();
    }
    
    /**
     * 清除当前消息
     */
    public static void clearMessage() {
        currentMessage = null;
        messageStartTime = 0;
    }
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (currentMessage == null) {
            return;
        }
        
        // 检查消息是否过期
        long currentTime = System.currentTimeMillis();
        if (currentTime - messageStartTime > MESSAGE_DURATION) {
            clearMessage();
            return;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 计算文本位置（物品栏上方）
        int textWidth = minecraft.font.width(currentMessage);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 69; // 物品栏上方位置
        
        // 计算透明度（淡入淡出效果）
        float alpha = calculateAlpha(currentTime - messageStartTime);
        int color = (int)(255 * alpha) << 24 | 0xFFFFFF; // 白色文本

        // 绘制文本
        guiGraphics.drawString(minecraft.font, currentMessage, x, y, color);
    }
    
    /**
     * 计算基于时间的透明度（淡入淡出效果）
     */
    private static float calculateAlpha(long elapsedTime) {
        float fadeInTime = 300f; // 300ms淡入
        float fadeOutTime = 500f; // 500ms淡出
        float totalTime = MESSAGE_DURATION;
        
        if (elapsedTime < fadeInTime) {
            // 淡入阶段
            return elapsedTime / fadeInTime;
        } else if (elapsedTime > totalTime - fadeOutTime) {
            // 淡出阶段
            return (totalTime - elapsedTime) / fadeOutTime;
        } else {
            // 完全显示阶段
            return 1.0f;
        }
    }
}
