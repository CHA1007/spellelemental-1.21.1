package com.chadate.spellelemental.element.attachment.environmental.handler;

import net.minecraft.world.entity.LivingEntity;

/**
 * 环境条件元素附着处理器接口
 */
public interface EnvironmentalAttachmentHandler {
    
    /**
     * 检查是否应该对指定实体应用环境元素附着
     * @param entity 目标实体
     * @return 如果满足环境条件则返回true
     */
    boolean shouldApply(LivingEntity entity);
    
    /**
     * 对实体应用环境元素附着效果
     * @param entity 目标实体
     */
    void applyEffect(LivingEntity entity);
    
    /**
     * 获取检查间隔（以tick为单位）
     * @return 检查间隔
     */
    int getCheckInterval();
    
    /**
     * 获取元素ID
     * @return 元素标识符
     */
    String getElementId();
}
