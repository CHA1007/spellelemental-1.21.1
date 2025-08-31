package com.chadate.spellelemental.util;

import com.chadate.spellelemental.register.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/**
 * 属性修饰符工具类
 * 提供便捷的属性修饰符操作方法
 */
public class AttributeModifierUtil {
    
    // 物理抗性下降修饰符的标识符
    public static final ResourceLocation PHYSICAL_RESISTANCE_DOWN_ID = 
        ResourceLocation.fromNamespaceAndPath("spellelemental", "physical_resistance_down");
    
    /**
     * 为实体添加物理抗性下降修饰符
     * @param entity 目标实体
     * @param amount 抗性下降量（负数）
     * @param duration 持续时间（tick），-1表示永久
     */
    public static void applyPhysicalResistanceDown(LivingEntity entity, double amount, int duration) {
        AttributeInstance instance = entity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
        if (instance == null) return;
        
        // 确保amount为负数（降低抗性）
        if (amount > 0) amount = -amount;
        
        // 移除已存在的修饰符
        removePhysicalResistanceDown(entity);
        
        // 添加新的修饰符
        AttributeModifier modifier = new AttributeModifier(
            PHYSICAL_RESISTANCE_DOWN_ID,
            amount,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
        
        instance.addPermanentModifier(modifier);
        
        // 如果有持续时间限制，设置移除任务
        if (duration > 0) {
            // 这里可以添加定时移除逻辑，或者通过其他系统管理
            // 例如与现有的元素衰减系统集成
        }
    }
    
    /**
     * 移除实体的物理抗性下降修饰符
     * @param entity 目标实体
     */
    public static void removePhysicalResistanceDown(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
        if (instance == null) return;
        
        instance.removeModifier(PHYSICAL_RESISTANCE_DOWN_ID);
    }
    
    /**
     * 检查实体是否有物理抗性下降修饰符
     * @param entity 目标实体
     * @return 是否有物理抗性下降效果
     */
    public static boolean hasPhysicalResistanceDown(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
        if (instance == null) return false;
        
        return instance.getModifier(PHYSICAL_RESISTANCE_DOWN_ID) != null;
    }
    
    /**
     * 获取实体当前的物理抗性下降值
     * @param entity 目标实体
     * @return 抗性下降值，0表示没有下降效果
     */
    public static double getPhysicalResistanceDownAmount(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
        if (instance == null) return 0.0;
        
        AttributeModifier modifier = instance.getModifier(PHYSICAL_RESISTANCE_DOWN_ID);
        return modifier != null ? Math.abs(modifier.amount()) : 0.0;
    }
    
    /**
     * 为实体添加临时物理抗性下降效果
     * 这个方法可以与现有的元素系统集成
     * @param entity 目标实体
     * @param percentage 抗性下降百分比（0.0-1.0）
     */
    public static void applyTemporaryPhysicalResistanceDown(LivingEntity entity, double percentage) {
        // 限制百分比范围
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        
        // 转换为修饰符值（负数）
        double modifierAmount = -percentage;
        
        applyPhysicalResistanceDown(entity, modifierAmount, -1);
    }
}
