package com.chadate.spellelemental.element.reaction.basic;


import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class DewSpark implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "nature_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0
                || "water_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        String damageSource = event.getSource().getMsgId();
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();

        if (DAMAGE_SOURCE_NATURE_MAGIC.equals(damageSource)) {
            applyDewsparkEffect(target, attackDamage, astralBlessing);
            ReactionEvent.ConsumeElement(event, "water", 200, "nature");
        } else if (DAMAGE_SOURCE_WATER_MAGIC.equals(damageSource)) {
            applyDewsparkEffect(target, attackDamage, astralBlessing);
            ReactionEvent.ConsumeElement(event, "nature", 200, "water");
        }
    }

    private void applyDewsparkEffect(LivingEntity target, float attackDamage, float astralBlessing) {
        target.getData(SpellAttachments.DEWSPARK_TIME).setValue(120);
        if (target.hasData(SpellAttachments.DEWSPARK_LAYERS)) {
            target.getData(SpellAttachments.DEWSPARK_LAYERS).setValue(target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue() + 1);
        } else {
            target.getData(SpellAttachments.DEWSPARK_LAYERS).setValue(1);
        }
        target.getData(SpellAttachments.DEWSPARK_DAMAGE).setValue((int) ReactionInjuryFormula.CalculateOverloadDamage(attackDamage, 1.75f, astralBlessing));
    }

    private static final String DAMAGE_SOURCE_NATURE_MAGIC = "nature_magic";
    private static final String DAMAGE_SOURCE_WATER_MAGIC = "water_magic";

}