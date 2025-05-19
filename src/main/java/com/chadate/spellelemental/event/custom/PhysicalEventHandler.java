package com.chadate.spellelemental.event.custom;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class PhysicalEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        PhysicalDamageEvent.PhysicalDamage(event);
    }
}