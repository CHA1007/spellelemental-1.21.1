package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import com.chadate.spellelemental.event.physical.PhysicalDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class Shatter implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ((PhysicalDamageEvent.isPhysicalDamage(source)) && target.getData(SpellAttachments.FREEZE_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        float electroDamage = ReactionInjuryFormula.CalculateOverloadDamage(attackDamage, 3f, astralBlessing);
        event.getEntity().hurt(attacker.damageSources().generic(), electroDamage);
        ReactionEvent.ConsumeElement(event, "freeze", 1000, "null");
    }
}