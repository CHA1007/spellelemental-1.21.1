package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class TriggerPromotion implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "nature_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.PROMOTION_ELEMENT).getValue() > 0
                || "lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.PROMOTION_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float originalDamage = event.getNewDamage();
        if ("lightning_magic".equals(event.getSource().getMsgId())) {
            float finalDamage = ReactionInjuryFormula.ElectroReactiveteDamage(originalDamage, 1.25f, astralBlessing);
            event.setNewDamage(finalDamage);
        } else if ("nature_magic".equals(event.getSource().getMsgId())) {
            float finalDamage = ReactionInjuryFormula.ElectroReactiveteDamage(originalDamage, 1.15f, astralBlessing);
            event.setNewDamage(finalDamage);
        }

    }
}
