package com.chadate.spellelemental.event.physical;

import com.chadate.spellelemental.attribute.ModAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class PhysicalEventHandler {

    @SubscribeEvent
    public static void applyPhysicalBonus(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        double originalDamage = event.getNewDamage();
        double physicalBoost = 1;

        if (isPhysicalDamage(source)) {
            // 安全获取攻击者，只有当攻击者是生物实体时才获取物理伤害加成
            if (source.getEntity() instanceof LivingEntity attacker) {
                var physicalBoostAttr = attacker.getAttribute(ModAttributes.PHYSICAL_DAMAGE_BOOST);
                if (physicalBoostAttr != null) {
                    physicalBoost = physicalBoostAttr.getValue();
                }
            }
            
            // 安全获取目标的物理抗性
            var physicalResistAttr = target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
            if (physicalResistAttr != null) {
                double physicalResist = physicalResistAttr.getValue();
                float finalDamage = (float) (originalDamage * physicalBoost * calculatePhysicalResistMultiplier(physicalResist));
                event.setNewDamage(finalDamage);
            }
        }
    }

    /**
     * 计算物理抗性倍数
     */
    private static double calculatePhysicalResistMultiplier(double physicalResist) {
        double resistanceMultiplier;
        if (physicalResist - 1 < 0) {
            resistanceMultiplier = 1 - ((physicalResist - 1) / 2);
        } else if (physicalResist - 1 < 0.75) {
            resistanceMultiplier = 1 - (physicalResist - 1);
        } else {
            resistanceMultiplier = 1 / (1 + 4 * (physicalResist - 1));
        }
        return resistanceMultiplier;
    }

    /**
     * 判断是否为物理伤害
     */
    private static boolean isPhysicalDamage(DamageSource source) {
        return source.is(Tags.DamageTypes.IS_PHYSICAL);
    }
}