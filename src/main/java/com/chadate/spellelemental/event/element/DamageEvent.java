package com.chadate.spellelemental.event.element;

import net.minecraft.world.damagesource.DamageSource;

public class DamageEvent {
    public static boolean isSpellDamage(DamageSource source) {
        if (source == null) return false;
        return source.getMsgId().endsWith("_magic");
    }
}
