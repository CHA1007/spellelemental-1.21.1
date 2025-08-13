package com.chadate.spellelemental.event.crit;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class CritDamageEvent {
    // 暴击处理主方法（仅对法术伤害生效）
    public static void handleCrit(LivingDamageEvent.Pre event, LivingEntity attacker) {
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

    public static boolean shouldTriggerSpellCrit(LivingEntity attacker) {
        double baseCritChance = attacker.getAttributeValue(ModAttributes.SPELL_CRIT_RATE);
        return Math.random() < baseCritChance;
    }

    // 法术暴击伤害计算
    public static double calculateSpellCritDamage(float originalDamage, LivingEntity attacker) {
        double critMultiplier = attacker.getAttributeValue(ModAttributes.SPELL_CRIT_DAMAGE);
        return originalDamage * (1 + critMultiplier);
    }

    // 暴击音效
    private static void triggerCritSound(LivingEntity attacker) {
        attacker.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }
}
