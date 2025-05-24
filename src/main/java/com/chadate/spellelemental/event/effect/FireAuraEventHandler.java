package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.FireAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class FireAuraEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        FireAuraEffect.applyFireAura(event);
    }
} 