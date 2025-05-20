package com.chadate.spellelemental.element.reaction.custom.fire;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class FireFreezeMeltReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "fire_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.FREEZE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float originalDamage = event.getNewDamage();
        float boostedDamage = originalDamage * (2 * ReactionEvent.CalculateBlessingBonus(astralBlessing));
        event.setNewDamage(boostedDamage);
        ReactionEvent.ConsumeElement(event, "freeze", 400);
    }
}