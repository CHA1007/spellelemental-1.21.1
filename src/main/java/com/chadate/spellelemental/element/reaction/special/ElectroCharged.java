package com.chadate.spellelemental.element.reaction.special;


import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.custom.SpecialElementReaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

public class ElectroCharged implements SpecialElementReaction {
    @Override
    public boolean appliesTo(EntityTickEvent.Pre event) {
        // 安全检查实体类型，只有生物实体才能有元素附着
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return false;
        }
        return (target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() != 0
                && target.getData(SpellAttachments.WATER_ELEMENT).getValue() != 0);
    }

    @Override
    public void apply(EntityTickEvent.Pre event) {
        // 安全获取生物实体
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (target.tickCount % 10 != 0) return;

        // 检索半径 1 格内的所有实体
        Level level = target.level();
        AABB area = target.getBoundingBox().inflate(1.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        // 对符合条件的实体造成伤害
        DamageSource damageSource = level.damageSources().lightningBolt();
        int damage = target.getData(SpellAttachments.ELECTRO_DAMAGE).getValue() | 5;

        for (LivingEntity entity : entities) {
            if (entity.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0) {
                entity.hurt(damageSource, damage);
            }
        }

        // 减少目标实体的水元素和雷元素附着值
        target.getData(SpellAttachments.WATER_ELEMENT).setValue(
                Math.max(0, target.getData(SpellAttachments.WATER_ELEMENT).getValue() - 100));
        target.getData(SpellAttachments.LIGHTNING_ELEMENT).setValue(
                Math.max(0, target.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() - 100));
    }
}
