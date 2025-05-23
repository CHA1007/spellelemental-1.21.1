package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class Melt implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ("fire_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.ICE_ELEMENT).getValue() > 0)
                || ("ice_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        String damageSource = event.getSource().getMsgId();
        float originalDamage = event.getNewDamage();
        if ("fire_magic".equals(damageSource)) {
            float boostedDamage = originalDamage * (2 * ReactionInjuryFormula.CalculateBlessingBonus(astralBlessing));
            event.setNewDamage(boostedDamage);
            ReactionEvent.ConsumeElement(event, "ice", 200, "fire");
        } else if ("ice_magic".equals(damageSource)) {
            float boostedDamage = (float) (originalDamage * (1.5 * ReactionInjuryFormula.CalculateBlessingBonus(astralBlessing)));
            event.setNewDamage(boostedDamage);
            ReactionEvent.ConsumeElement(event, "fire", 100, "ice");
        }
    }
}
