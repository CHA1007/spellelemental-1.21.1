package com.chadate.spellelemental.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashSet;
import java.util.Set;


public class OverloadReactionHandler {
    // 存储已经受到伤害的实体 ID
    private static final Set<Integer> damagedEntities = new HashSet<>();

    public void applyOverloadDamage(LivingEntity target, DamageSource source, int damage) {
        if (damagedEntities.contains(target.getId())) {
            return; // 实体已经受到伤害，跳过
        }

        // 应用伤害
        target.hurt(source, damage);

        // 标记实体已受伤害
        damagedEntities.add(target.getId());
    }

    // 在每个 Tick 结束时清除记录
    @SubscribeEvent
    public void onServerTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 !=  0) return;
        damagedEntities.clear();
    }
}