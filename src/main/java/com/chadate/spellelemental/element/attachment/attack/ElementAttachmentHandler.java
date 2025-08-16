package com.chadate.spellelemental.element.attachment.attack;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface ElementAttachmentHandler {
    boolean canApply(LivingEntity target, DamageSource source, float damageAmount);
    void applyEffect(LivingEntity target, DamageSource source, int entityId);
}