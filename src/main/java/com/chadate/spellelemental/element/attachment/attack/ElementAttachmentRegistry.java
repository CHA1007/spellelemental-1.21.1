package com.chadate.spellelemental.element.attachment.attack;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 元素附着注册表
 * 支持动态注册和清空处理器，用于数据包驱动系统
 */
public class ElementAttachmentRegistry {
    private static final List<ElementAttachmentHandler> handlers = new ArrayList<>();
    private static final AtomicReference<String> latestAppliedElement = new AtomicReference<>("");

    /**
     * 注册元素附着处理器
     */
    public static void register(ElementAttachmentHandler handler) {
        handlers.add(handler);
        // SpellElemental.LOGGER.debug("Registered element attachment handler: {}", 
        //     handler.getClass().getSimpleName());
    }

    /**
     * 清空所有已注册的处理器
     * 用于数据包重载时重新注册
     */
    public static void clearHandlers() {
        handlers.clear();
    }

    /**
     * 处理元素附着逻辑
     * 遍历所有注册的处理器，找到第一个匹配的进行处理
     */
    public static void handleAttachment(LivingEntity target, DamageSource source, int entityId) {
        for (ElementAttachmentHandler handler : handlers) {
            if (handler.canApply(target, source)) {
                handler.applyEffect(target, source, entityId);
                
                // 记录最后应用的元素（如果是动态处理器）
                if (handler instanceof DynamicElementHandler) {
                    String attachmentType = ((DynamicElementHandler) handler).getConfig().getAttachmentType();
                    latestAppliedElement.set(attachmentType);
                }
                
                break; // 只应用第一个匹配的处理器
            }
        }
    }

    /**
     * 获取最后应用的元素ID
     */
    public static String getLatestAppliedElement() {
        return latestAppliedElement.get();
    }

    /**
     * 获取当前注册的处理器数量
     */
    public static int getHandlerCount() {
        return handlers.size();
    }

    /**
     * 获取所有注册的处理器（只读）
     */
    public static List<ElementAttachmentHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}