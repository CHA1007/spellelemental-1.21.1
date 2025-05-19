package com.chadate.spellelemental.element.reaction.custom.fire;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class FireDeflagrationReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "fire_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        double attackDamage = Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        ReactionEvent.FireAreaDamage(event.getEntity(), 3, attacker, attackDamage, 2.75f, astralBlessing);
        ReactionEvent.ConsumeElement(event, "lightning", 200, "fire");
    }
}