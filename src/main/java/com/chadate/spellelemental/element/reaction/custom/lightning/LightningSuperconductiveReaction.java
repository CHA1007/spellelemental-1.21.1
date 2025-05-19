package com.chadate.spellelemental.element.reaction.custom.lightning;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class LightningSuperconductiveReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "lightning_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.ICE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        double attackDamage = Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        ReactionEvent.FreezeAreaDamage(target, 3, attacker, attackDamage, 1.5f, astralBlessing);

        boolean hasVulnerable = Objects.requireNonNull(target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST))
                .hasModifier(ResourceLocation.parse("vulnerable"));

        if (!hasVulnerable) {
            Objects.requireNonNull(target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST)).addTransientModifier(
                    new AttributeModifier(ResourceLocation.parse("vulnerable"), -0.6, AttributeModifier.Operation.ADD_VALUE));
            event.getEntity().getData(SpellAttachments.VULNERABILITY_ELEMENT).setValue(240);
        }

        ReactionEvent.ConsumeElement(event, "ice", 200, "lightning");
    }
}