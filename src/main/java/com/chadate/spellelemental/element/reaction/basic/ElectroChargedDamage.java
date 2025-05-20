package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class ElectroChargedDamage implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ("lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0)
                || ("water_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        float electroDamage = ReactionEvent.CalculateOverloadDamage(attackDamage, 2.0f, astralBlessing);
        event.getEntity().getData(SpellAttachments.ELECTRO_DAMAGE).setValue((int) electroDamage);
    }
}
