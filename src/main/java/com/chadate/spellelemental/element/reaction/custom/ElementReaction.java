package com.chadate.spellelemental.element.reaction.custom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public interface ElementReaction {
    boolean appliesTo(LivingEntity target, DamageSource source);
    void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing);
}