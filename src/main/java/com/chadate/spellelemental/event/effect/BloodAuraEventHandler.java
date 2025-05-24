package com.chadate.spellelemental.event.effect;

import com.chadate.spellelemental.event.effect.effect.BloodAuraEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.chadate.spellelemental.SpellElemental;

public class BloodAuraEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        BloodAuraEffect.applyBloodAura(event);
    }
} 