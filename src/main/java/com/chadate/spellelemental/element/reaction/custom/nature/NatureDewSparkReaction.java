package com.chadate.spellelemental.element.reaction.custom.nature;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class NatureDewSparkReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "nature_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        target.getData(SpellAttachments.DEWSPARK_TIME).setValue(120);
        if (target.hasData(SpellAttachments.DEWSPARK_LAYERS)){
            target.getData(SpellAttachments.DEWSPARK_LAYERS).setValue(target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue() + 1);
            target.getData(SpellAttachments.DEWSPARK_DAMAGE).setValue((int) ReactionEvent.CalculateOverloadDamage(attackDamage, 1.75f, astralBlessing));
        }else {
            target.getData(SpellAttachments.DEWSPARK_LAYERS).setValue(1);
            target.getData(SpellAttachments.DEWSPARK_DAMAGE).setValue((int) ReactionEvent.CalculateOverloadDamage(attackDamage, 1.75f, astralBlessing));
        }
        ReactionEvent.ConsumeElement(event, "water", 200, "nature");
    }
}