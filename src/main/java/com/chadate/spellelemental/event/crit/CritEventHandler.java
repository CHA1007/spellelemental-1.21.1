package com.chadate.spellelemental.event.crit;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class CritEventHandler {
    @SubscribeEvent
    public static void applyCritBonus(LivingDamageEvent.Pre event) {
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        CritDamageEvent.handleCrit(event, attacker);
    }
}