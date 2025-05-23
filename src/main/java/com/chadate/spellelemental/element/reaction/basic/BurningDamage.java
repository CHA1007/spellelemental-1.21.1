package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class BurningDamage implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "nature_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0
                || "fire_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        float burnDamage = ReactionInjuryFormula.CalculateOverloadDamage(attackDamage, 1.5f, astralBlessing);
        event.getEntity().getData(SpellAttachments.BURN_DAMAGE).setValue((int) burnDamage);
    }
}