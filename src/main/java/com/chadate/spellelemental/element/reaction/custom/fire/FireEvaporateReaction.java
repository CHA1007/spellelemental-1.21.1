package com.chadate.spellelemental.element.reaction.custom.fire;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class FireEvaporateReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "fire_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float originalDamage = event.getOriginalDamage();
        float boostedDamage = (float) (originalDamage * (1.5 * ReactionEvent.CalculateBlessingBonus(astralBlessing)));
        event.setNewDamage(boostedDamage);
        System.out.println("2");
        ReactionEvent.ConsumeElement(event, "water", 200, "fire");
    }
}