package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.NatureAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class NatureAuraEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        NatureAuraEffect.applyNatureAura(event);
    }
} 