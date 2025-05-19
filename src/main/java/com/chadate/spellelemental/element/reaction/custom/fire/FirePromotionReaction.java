package com.chadate.spellelemental.element.reaction.custom.fire;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.ElementReaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class FirePromotionReaction implements ElementReaction {
    @Override
    public boolean appliesTo(LivingEntity target, DamageSource source) {
        return  "fire_magic".equals(source.getMsgId())
                && target.getData(SpellAttachments.PROMOTION_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        LivingEntity target = event.getEntity();
        int promotionElement = target.getData(SpellAttachments.PROMOTION_ELEMENT).getValue();
        target.getData(SpellAttachments.NATURE_ELEMENT).setValue(promotionElement);
        target.getData(SpellAttachments.PROMOTION_ELEMENT).setValue(0);
    }
}
