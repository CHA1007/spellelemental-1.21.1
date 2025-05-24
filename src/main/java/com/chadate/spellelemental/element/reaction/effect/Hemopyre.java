package com.chadate.spellelemental.element.reaction.effect;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.entity.status.ApplayStatus;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class Hemopyre implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ("fire_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.BLOOD_ELEMENT).getValue() > 0)
                || ("blood_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        String damageSource = event.getSource().getMsgId();
        float originalDamage = event.getNewDamage();
        if ("fire_magic".equals(damageSource)) {
            float boostedDamage = (float) (originalDamage * (1.2 * ReactionInjuryFormula.CalculateBlessingBonus(astralBlessing)));
            attacker.heal(boostedDamage * 0.1f);
            event.setNewDamage(boostedDamage);
            ReactionEvent.ConsumeElement(event, "blood", 200, "fire");
        } else if ("blood_magic".equals(damageSource)) {
            float boostedDamage = (float) (originalDamage * (1.2 * ReactionInjuryFormula.CalculateBlessingBonus(astralBlessing)));
            ApplayStatus.applyToReduceTheTherapeuticEffect(event, -0.5f, AttributeModifier.Operation.ADD_VALUE, 240);
            event.setNewDamage(boostedDamage);
            ReactionEvent.ConsumeElement(event, "fire", 100, "blood");
        }
    }
}
