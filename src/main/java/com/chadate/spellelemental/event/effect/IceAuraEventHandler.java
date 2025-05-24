package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.IceAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class IceAuraEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        IceAuraEffect.applyIceAura(event);
    }
} 