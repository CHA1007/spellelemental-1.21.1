package com.chadate.spellelemental.util;

import com.chadate.spellelemental.register.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.common.Tags;

/**
 * 物理伤害计算工具类
 * 提供优化的物理伤害计算逻辑
 */
public class PhysicalDamageCalculator {
    
    /**
     * 物理伤害计算结果
     */
    public record PhysicalDamageResult(
            float finalDamage,
            float damageMultiplier,
            float resistanceMultiplier,
            boolean wasModified
    ) {}
    
    /**
     * 计算物理伤害的最终结果
     */
    public static PhysicalDamageResult calculateDamage(
            float originalDamage,
            LivingEntity attacker,
            LivingEntity target,
            DamageSource source) {
        
        if (!isPhysicalDamage(source)) {
            return new PhysicalDamageResult(originalDamage, 1.0f, 1.0f, false);
        }
        
        // 获取攻击者的物理伤害加成
        float damageBoost = getAttributeValue(attacker, ModAttributes.PHYSICAL_DAMAGE_BOOST);
        // 获取目标的物理抗性
        float resistance = getAttributeValue(target, ModAttributes.PHYSICAL_DAMAGE_RESIST);
        // 计算抗性倍数 - 使用优化的递减收益公式
        float resistanceMultiplier = calculateResistanceMultiplier(resistance);
        
        // 计算最终伤害
        float finalDamage = originalDamage * damageBoost * resistanceMultiplier;
        
        return new PhysicalDamageResult(
                finalDamage,
                damageBoost,
                resistanceMultiplier,
                true
        );
    }
    
    /**
     * 优化的物理抗性计算公式
     * 使用递减收益模型，提供平衡的抗性效果
     */
    public static float calculateResistanceMultiplier(float resistance) {
        float delta = resistance - 1.0f;
        
        if (delta < 0) {
            // 负抗性：线性增伤，但限制最大增伤倍数
            return Math.min(2.0f, 1.0f - (delta * 0.5f));
        } else if (delta < 0.75f) {
            // 低抗性：线性减伤
            return Math.max(0.1f, 1.0f - delta);
        } else {
            // 高抗性：递减公式，防止完全免疫
            return Math.max(0.05f, 1.0f / (1.0f + 4.0f * delta));
        }
    }
    
    /**
     * 安全获取属性值
     */
    private static float getAttributeValue(LivingEntity entity, 
                                         Holder<Attribute> attribute) {
        if (entity == null) return 1.0f;
        
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? (float) instance.getValue() : 1.0f;
    }
    
    /**
     * 判断是否为物理伤害
     */
    private static boolean isPhysicalDamage(DamageSource source) {
        return source.is(Tags.DamageTypes.IS_PHYSICAL);
    }
}
