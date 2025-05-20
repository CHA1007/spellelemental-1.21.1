package com.chadate.spellelemental.element.reaction.special;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.SpecialElementReaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ElectroCharged implements SpecialElementReaction {
    @Override
    public boolean appliesTo(EntityTickEvent.Pre event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        return (target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() != 0
                && target.getData(SpellAttachments.WATER_ELEMENT).getValue() != 0);
    }

    @Override
    public void apply(EntityTickEvent.Pre event) {
        LivingEntity target = (LivingEntity) event.getEntity();
        if (target.tickCount % 10 != 0) return ;
        DamageSource damageSource = event.getEntity().level().damageSources().lightningBolt();
        int damage = target.getData(SpellAttachments.ELECTRO_DAMAGE).getValue() | 5;
        event.getEntity().hurt(damageSource, damage);
    }
}
