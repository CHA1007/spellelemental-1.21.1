package com.chadate.spellelemental.event.heal;

import com.chadate.spellelemental.attribute.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

public class HealingEventHandler {
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        AttributeInstance healingPower = entity.getAttribute(ModAttributes.HEALING_POWER);
        if (healingPower != null) {
            float baseHealing = event.getAmount();
            float finalHealing = baseHealing * (float) healingPower.getValue();
            event.setAmount(finalHealing); // 修改治疗数值
        }
    }
}
