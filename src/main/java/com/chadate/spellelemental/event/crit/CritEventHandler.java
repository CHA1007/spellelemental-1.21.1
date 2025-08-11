package com.chadate.spellelemental.event.crit;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class CritEventHandler {
    @SubscribeEvent
    public static void applyCritBonus(LivingDamageEvent.Pre event) {
        // 安全获取攻击者，只有当攻击者是生物实体时才处理暴击
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            CritDamageEvent.handleCrit(event, attacker);
        }
    }
}