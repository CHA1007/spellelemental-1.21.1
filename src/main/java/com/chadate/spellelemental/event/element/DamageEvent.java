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

    public static boolean IsInFireDamage(LivingDamageEvent.Pre event) {
        return isInFireDamage(event.getSource());
    }
    public static boolean isInFireDamage(DamageSource source) {
        if (source == null) return false;
        return source.getMsgId().contains("inFire");
    }

    //检测伤害来源是否为实体
    public static boolean IsEntityDamage(LivingDamageEvent.Pre event) {
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        return attacker != null;
    }

    public static void CancelSpellUnbeatableFrames(LivingEntity target) {
        target.invulnerableTime = 0;
    }

    public static void setDamageUnbeatable(LivingEntity target, int duration) {
        target.invulnerableTime = duration;
    }
}
