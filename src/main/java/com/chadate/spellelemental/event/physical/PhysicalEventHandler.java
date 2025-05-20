package com.chadate.spellelemental.event.physical;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class PhysicalEventHandler {

    @SubscribeEvent
    public static void applyPhysicalBonus(LivingDamageEvent.Pre event) {
        PhysicalDamageEvent.PhysicalDamage(event);
    }
}