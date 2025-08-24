package com.chadate.spellelemental;


import com.chadate.spellelemental.client.hud.ElementDebugOverlay;
import com.chadate.spellelemental.client.render.ElementAuraRenderer;
import com.chadate.spellelemental.client.render.ElementIconRenderer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 客户端专用初始化类
 * 负责注册所有客户端专用的事件监听器和渲染器
 */
public class SpellElementalClient {

    /**
     * 客户端初始化入口点
     * 在 FMLClientSetupEvent 阶段被调用
     */
    public static void init() {
        registerClientEvents();
    }

    /**
     * 注册客户端专用事件监听器
     */
    private static void registerClientEvents() {
        // 注册渲染事件监听器
        NeoForge.EVENT_BUS.addListener(ElementIconRenderer::onRenderLivingPost);
        NeoForge.EVENT_BUS.addListener(ElementAuraRenderer::onRenderLivingPost);
        NeoForge.EVENT_BUS.addListener(ElementDebugOverlay::onRenderGui);
    }

    /**
     * 客户端设置阶段的额外初始化
     * 可以在这里添加其他客户端专用的设置
     */
    public static void setup() {
    }
}
