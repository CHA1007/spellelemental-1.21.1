package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
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
        String damageSource = event.getSource().getMsgId();
        if ("lightning_magic".equals(damageSource)) {
            target.getData(SpellAttachments.PROMOTION_ELEMENT).setValue(target.getData(SpellAttachments.NATURE_ELEMENT).getValue());
            ReactionEvent.ConsumeElement(event, "nature", 200, "lightning");
        } else if ("nature_magic".equals(damageSource)) {
            target.getData(SpellAttachments.PROMOTION_ELEMENT).setValue(target.getData(SpellAttachments.NATURE_ELEMENT).getValue());
            ReactionEvent.ConsumeElement(event, "lightning", 200, "nature");
        }
    }
}
