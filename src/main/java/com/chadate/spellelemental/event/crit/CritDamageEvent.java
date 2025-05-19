package com.chadate.spellelemental.event.crit;

import com.chadate.spellelemental.attribute.ModAttributes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class CritDamageEvent {
    // 暴击处理主方法
    public static void handleCrit(LivingDamageEvent.Pre event, LivingEntity attacker) {
        if (attacker == null) return;

        // 判定是否触发暴击
        if (shouldTriggerCrit(attacker)) {

            float critDamage = (float) calculateCritDamage(event.getNewDamage(), attacker);
            event.setNewDamage(critDamage);

            // 触发音效
            triggerCritSound(attacker);
        }
    }

    public static boolean shouldTriggerCrit(LivingEntity attacker) {
        double baseCritChance = attacker.getAttributeValue(ModAttributes.CRIT_RATE);

        return Math.random() < baseCritChance;
    }

    // 暴击伤害计算
    public static double calculateCritDamage(float originalDamage, LivingEntity attacker) {
        double critMultiplier = attacker.getAttributeValue(ModAttributes.CRIT_DAMAGE);

        return originalDamage * (1 +critMultiplier);
    }

    // 暴击音效
    private static void triggerCritSound(LivingEntity attacker) {
        attacker.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }
}
