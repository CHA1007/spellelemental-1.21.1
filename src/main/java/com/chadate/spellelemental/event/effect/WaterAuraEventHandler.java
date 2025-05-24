package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.WaterAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class WaterAuraEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        WaterAuraEffect.applyWaterAura(event);
    }
} 