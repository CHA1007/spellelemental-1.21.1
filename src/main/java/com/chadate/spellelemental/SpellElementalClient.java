package com.chadate.spellelemental;


import com.chadate.spellelemental.client.hud.ElementDebugOverlay;
import com.chadate.spellelemental.client.render.ElementAuraRenderer;
import com.chadate.spellelemental.client.render.ElementIconRenderer;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端专用初始化类
 * 负责注册所有客户端专用的事件监听器和渲染器
 */
public class SpellElementalClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellElementalClient.class);

    /**
     * 客户端初始化入口点
     * 在 FMLClientSetupEvent 阶段被调用
     */
    public static void init() {
        LOGGER.info("[SpellElemental] Starting client initialization...");
        
        registerClientEvents();
        
        LOGGER.info("[SpellElemental] Client initialization completed successfully!");
    }

    /**
     * 注册客户端专用事件监听器
     */
    private static void registerClientEvents() {
        LOGGER.info("[SpellElemental] Registering client-side event listeners...");
        
        // 注册渲染事件监听器
        NeoForge.EVENT_BUS.addListener(ElementIconRenderer::onRenderLivingPost);
        NeoForge.EVENT_BUS.addListener(ElementAuraRenderer::onRenderLivingPost);
        NeoForge.EVENT_BUS.addListener(ElementDebugOverlay::onRenderGui);
        
        LOGGER.info("[SpellElemental] Registered {} client event listeners", 3);
        LOGGER.info("[SpellElemental] - ElementIconRenderer::onRenderLivingPost");
        LOGGER.info("[SpellElemental] - ElementAuraRenderer::onRenderLivingPost");
        LOGGER.info("[SpellElemental] - ElementDebugOverlay::onRenderGui");
    }

    /**
     * 客户端设置阶段的额外初始化
     * 可以在这里添加其他客户端专用的设置
     */
    public static void setup() {
        LOGGER.info("[SpellElemental] Client setup phase completed");
    }
}
