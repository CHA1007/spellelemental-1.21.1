package com.chadate.spellelemental.element.reaction.basic;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.ElementReaction;
import com.chadate.spellelemental.event.element.DamageEvent;
import com.chadate.spellelemental.event.element.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class TriggerDewSpark implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "lightning_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue() > 0
                || "fire_magic".equals(source.getMsgId()) && target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        String damageSource = event.getSource().getMsgId();
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        int dewsparkLayers = target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue();
        if ("fire_magic".equals(damageSource)) {
            ReactionEvent.MagicAreaDamage(target, 3f, attacker, attackDamage, 2.5f * dewsparkLayers, astralBlessing);
            ReactionEvent.ConsumeElement(event, "dewspark", 10);
            target.removeData(SpellAttachments.DEWSPARK_TIME);

        } else if ("lightning_magic".equals(damageSource)) {
            float overloadDamage = ReactionEvent.CalculateOverloadDamage(attackDamage, 3f, astralBlessing);
            DamageSource damageSourceAttack = attacker.damageSources().magic();
            target.hurt(damageSourceAttack, overloadDamage * dewsparkLayers);
            ReactionEvent.ConsumeElement(event, "dewspark", 10);
            target.removeData(SpellAttachments.DEWSPARK_TIME);

        }
    }
}