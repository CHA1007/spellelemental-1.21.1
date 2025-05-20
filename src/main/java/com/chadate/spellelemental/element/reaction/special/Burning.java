package com.chadate.spellelemental.element.reaction.special;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.SpecialElementReaction;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class Burning implements SpecialElementReaction {

    @Override
    public boolean appliesTo(EntityTickEvent.Pre event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        return target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0
                && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(EntityTickEvent.Pre event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        if (target.tickCount % 5 != 0) return ;
        int damage = target.getData(SpellAttachments.BURN_DAMAGE).getValue() | 5;
        target.hurt(target.level().damageSources().inFire(), damage);
        DamageEvent.CancelSpellUnbeatableFrames(target);
        target.getData(SpellAttachments.FIRE_ELEMENT).setValue(200);
    }
}
