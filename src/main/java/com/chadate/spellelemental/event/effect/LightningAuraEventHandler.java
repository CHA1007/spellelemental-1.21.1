package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.LightningAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class LightningAuraEventHandler {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        LightningAuraEffect.applyLightningAura(event);
    }
} 