package com.chadate.spellelemental.element.reaction.special;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.SpecialElementReaction;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class Burning implements SpecialElementReaction {

    @Override
    public boolean appliesTo(EntityTickEvent.Pre event) {
        // 安全检查实体类型，只有生物实体才能有元素附着
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return false;
        }
        return target.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0
                && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() > 0;
    }

    @Override
    public void apply(EntityTickEvent.Pre event) {
        // 安全获取生物实体
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (target.tickCount % 5 != 0) return ;
        int damage = target.getData(SpellAttachments.BURN_DAMAGE).getValue() | 5;
        target.hurt(target.level().damageSources().inFire(), damage);
        DamageEvent.CancelSpellUnbeatableFrames(target);
        target.getData(SpellAttachments.FIRE_ELEMENT).setValue(200);
    }
}
