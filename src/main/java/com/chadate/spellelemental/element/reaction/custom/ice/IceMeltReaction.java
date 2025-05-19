package com.chadate.spellelemental.element.reaction.custom.ice;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class IceMeltReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "ice_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float originalDamage = event.getOriginalDamage();
        float boostedDamage = (float) (originalDamage * (1.5 * ReactionEvent.CalculateBlessingBonus(astralBlessing)));
        event.setNewDamage(boostedDamage);
        ReactionEvent.ConsumeElement(event, "fire", 100, "ice");
    }
}