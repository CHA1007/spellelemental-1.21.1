package com.chadate.spellelemental.element.reaction.custom.lightning;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import com.chadate.spellelemental.event.ReactionEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class LightningSurgechargeReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "lightning_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        float attackDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();

        float overloadDamage = ReactionEvent.CalculateOverloadDamage(attackDamage, 3f, astralBlessing);
        int dewsparkLayers = target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue();

        DamageSource damageSource = attacker.damageSources().magic();
        target.hurt(damageSource, overloadDamage * dewsparkLayers);

    }
}
