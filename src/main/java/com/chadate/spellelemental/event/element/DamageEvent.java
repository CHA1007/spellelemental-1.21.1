package com.chadate.spellelemental.event.element;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class DamageEvent {
    public static boolean IsSpellDamage(LivingDamageEvent.Pre event) {
        return isSpellDamage(event.getSource());
    }
    public static boolean isSpellDamage(DamageSource source) {
        if (source == null) return false;
        return source.getMsgId().endsWith("_magic");
    }
}
