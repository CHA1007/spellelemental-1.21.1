package com.chadate.spellelemental.element.reaction.reaction;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class Overload implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ("fire_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0)
                || ("lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        String damageSource = event.getSource().getMsgId();
        double attackDamage = Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        if ("fire_magic".equals(damageSource)) {
            ReactionEvent.FireAreaDamage(event.getEntity(), 3, attacker, attackDamage, 2.75f, astralBlessing);
            ReactionEvent.ConsumeElement(event, "lightning", 200);
        }else if ("lightning_magic".equals(damageSource)){
            ReactionEvent.FireAreaDamage(event.getEntity(), 3, attacker, attackDamage, 2.75f, astralBlessing);
            ReactionEvent.ConsumeElement(event, "fire", 200);
        }
    }
}
