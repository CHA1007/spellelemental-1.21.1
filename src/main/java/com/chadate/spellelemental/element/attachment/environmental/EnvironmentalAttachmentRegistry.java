package com.chadate.spellelemental.element.attachment.environmental;

import com.chadate.spellelemental.element.attachment.environmental.handler.EnvironmentalAttachmentHandler;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境条件元素附着注册表
 * 管理所有环境条件处理器的注册和执行
 */
public class EnvironmentalAttachmentRegistry {
    
    private static final List<EnvironmentalAttachmentHandler> handlers = new ArrayList<>();
    private static final Map<String, Integer> lastCheckTicks = new HashMap<>();
    
    /**
     * 注册环境条件处理器
     * @param handler 处理器实例
     */
    public static void register(EnvironmentalAttachmentHandler handler) {
        handlers.add(handler);
        lastCheckTicks.put(handler.getElementId(), 0);
    }
    
    /**
     * 清空所有处理器
     */
    public static void clearHandlers() {
        handlers.clear();
        lastCheckTicks.clear();
    }
    
    /**
     * 处理实体的环境条件检查
     * @param entity 目标实体
     * @param currentTick 当前游戏tick
     */
    public static void handleEnvironmentalCheck(LivingEntity entity, int currentTick) {
        for (EnvironmentalAttachmentHandler handler : handlers) {
            String elementId = handler.getElementId();
            int lastCheck = lastCheckTicks.getOrDefault(elementId, 0);
            int interval = handler.getCheckInterval();
            
            // 检查是否到了检查时间
            if (currentTick - lastCheck >= interval) {
                if (handler.shouldApply(entity)) {
                    handler.applyEffect(entity);
                }
                lastCheckTicks.put(elementId, currentTick);
            }
        }
    }
    
    /**
     * 获取已注册的处理器数量
     * @return 处理器数量
     */
    public static int getHandlerCount() {
        return handlers.size();
    }
    
    /**
     * 获取所有已注册的元素ID
     * @return 元素ID列表
     */
    public static List<String> getRegisteredElementIds() {
        return handlers.stream()
                .map(EnvironmentalAttachmentHandler::getElementId)
                .toList();
    }
}
