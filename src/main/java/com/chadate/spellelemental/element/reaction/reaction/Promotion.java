package com.chadate.spellelemental.element.reaction.reaction;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class Promotion implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0
                || "nature_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        target.getData(SpellAttachments.PROMOTION_ELEMENT).setValue(target.getData(SpellAttachments.NATURE_ELEMENT).getValue());
        ReactionEvent.ConsumeElement(event, "nature", 200);
        ReactionEvent.ConsumeElement(event, "lightning", 200);
    }
}
