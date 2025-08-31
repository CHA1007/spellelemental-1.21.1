package com.chadate.spellelemental.event.physical;

import com.chadate.spellelemental.util.PhysicalDamageCalculator;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class PhysicalEventHandler {

    public static void applyPhysicalBonus(LivingDamageEvent.Pre event) {

        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        float originalDamage = event.getNewDamage();

        // 获取攻击者
        LivingEntity attacker = null;
        if (source.getEntity() instanceof LivingEntity livingAttacker) {
            attacker = livingAttacker;
        }

        // 使用物理伤害计算系统
        PhysicalDamageCalculator.PhysicalDamageResult result =
                PhysicalDamageCalculator.calculateDamage(originalDamage, attacker, target, source);

        if (result.wasModified()) {
            event.setNewDamage(result.finalDamage());
        }
    }
}