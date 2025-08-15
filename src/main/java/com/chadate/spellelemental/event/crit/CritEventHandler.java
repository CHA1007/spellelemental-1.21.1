package com.chadate.spellelemental.event.crit;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class CritEventHandler {
    
    @SubscribeEvent
    public static void applyCritBonus(LivingDamageEvent.Pre event) {
        // 安全获取攻击者，只有当攻击者是生物实体时才处理暴击
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            handleCrit(event, attacker);
        }
    }

    /**
     * 暴击处理主方法（仅对法术伤害生效）
     */
    private static void handleCrit(LivingDamageEvent.Pre event, LivingEntity attacker) {
        if (attacker == null) return;

        // 仅当伤害为法术伤害时，才进行暴击判定与结算
        if (!DamageEvent.IsSpellDamage(event)) return;

        // 判定是否触发法术暴击
        if (shouldTriggerSpellCrit(attacker)) {
            float critDamage = (float) calculateSpellCritDamage(event.getNewDamage(), attacker);
            event.setNewDamage(critDamage);

            // 触发音效
            triggerCritSound(attacker);
        }
    }

    /**
     * 判断是否触发法术暴击
     */
    private static boolean shouldTriggerSpellCrit(LivingEntity attacker) {
        double baseCritChance = attacker.getAttributeValue(ModAttributes.SPELL_CRIT_RATE);
        return Math.random() < baseCritChance;
    }

    /**
     * 法术暴击伤害计算
     */
    private static double calculateSpellCritDamage(float originalDamage, LivingEntity attacker) {
        double critMultiplier = attacker.getAttributeValue(ModAttributes.SPELL_CRIT_DAMAGE);
        return originalDamage * (1 + critMultiplier);
    }

    /**
     * 暴击音效
     */
    private static void triggerCritSound(LivingEntity attacker) {
        attacker.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }
}