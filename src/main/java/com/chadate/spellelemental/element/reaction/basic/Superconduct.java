package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.entity.status.ApplayStatus;
import com.chadate.spellelemental.event.element.ReactionEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class Superconduct implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  ("lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.ICE_ELEMENT).getValue() > 0)
                || ("ice_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() > 0);
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        String damageSource = event.getSource().getMsgId();
        LivingEntity target = event.getEntity();
        double attackDamage = Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        ReactionEvent.FreezeAreaDamage(target, 1, attacker, attackDamage, 1.5f, astralBlessing);
        ApplayStatus.applyVulnerability(event, -0.6f, AttributeModifier.Operation.ADD_VALUE, 240);

        if ("lightning_magic".equals(damageSource)) {
            ReactionEvent.ConsumeElement(event, "ice", 200, "lightning");
        } else if ("ice_magic".equals(damageSource)) {
            ReactionEvent.ConsumeElement(event, "lightning", 200,  "ice");
        }
    }
}