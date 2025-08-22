package com.chadate.spellelemental.event.crit;

import com.chadate.spellelemental.register.ModAttributes;
import com.chadate.spellelemental.register.ModSounds;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class SpellDamageCritHandler {

    public static void applyCritBonus(SpellDamageEvent event) {
        // 安全获取攻击者，只有当攻击者是生物实体时才处理暴击
        if (event.getSpellDamageSource().getEntity() instanceof LivingEntity attacker) {
            handleCrit(event, attacker);
        }
    }

    /**
     * 暴击处理主方法（仅对法术伤害生效）
     */
    private static void handleCrit(SpellDamageEvent event, LivingEntity attacker) {
        if (attacker == null) return;

        // 判定是否触发法术暴击
        if (shouldTriggerSpellCrit(attacker)) {
            float critDamage = (float) calculateSpellCritDamage(event.getOriginalAmount(), attacker);
            event.setAmount(critDamage);

            // 触发音效
//            triggerCritSound(attacker);
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
        SoundSource soundSource = attacker.getSoundSource();
        // 仅让触发暴击的攻击者本人听到
        if (!attacker.level().isClientSide() && attacker instanceof ServerPlayer sp) {
            sp.playNotifySound(
                    ModSounds.SPELL_CRIT.get(),
                    soundSource,
                    1.0f,
                    1.0f
            );
        }
    }
}