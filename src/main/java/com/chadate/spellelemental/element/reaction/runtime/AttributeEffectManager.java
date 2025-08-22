package com.chadate.spellelemental.element.reaction.runtime;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性效果管理器 - 负责管理有持续时间的属性修饰符
 */
public class AttributeEffectManager {
    
    /**
     * 活跃的属性效果记录
     * Key: 实体ID, Value: 该实体的属性效果列表
     */
    private static final Map<Integer, List<ActiveAttributeEffect>> ACTIVE_EFFECTS = new ConcurrentHashMap<>();
    
    /**
     * 活跃属性效果数据结构
     */
    public static class ActiveAttributeEffect {
        public final int entityId;
        public final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attributeHolder;
        public final ResourceLocation modifierId;
        public final long expireTime; // 过期时间（游戏时间）
        
        public ActiveAttributeEffect(int entityId, 
                                   net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attributeHolder,
                                   ResourceLocation modifierId, 
                                   long expireTime) {
            this.entityId = entityId;
            this.attributeHolder = attributeHolder;
            this.modifierId = modifierId;
            this.expireTime = expireTime;
        }
    }
    
    /**
     * 注册一个有持续时间的属性效果，如果已存在相同修饰符则刷新持续时间
     */
    public static void registerTimedEffect(LivingEntity entity,
                                         net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attributeHolder,
                                         ResourceLocation modifierId,
                                         int durationTicks) {
        if (entity == null || entity.level().isClientSide() || durationTicks <= 0) return;
        
        long currentTime = entity.level().getGameTime();
        long expireTime = currentTime + durationTicks;
        int entityId = entity.getId();
        
        List<ActiveAttributeEffect> effects = ACTIVE_EFFECTS.computeIfAbsent(entityId, k -> new ArrayList<>());
        
        // 检查是否已存在相同的修饰符，如果存在则刷新时间
        boolean found = false;
        for (int i = 0; i < effects.size(); i++) {
            ActiveAttributeEffect existing = effects.get(i);
            if (existing.modifierId.equals(modifierId) && existing.attributeHolder.equals(attributeHolder)) {
                // 找到相同修饰符，更新过期时间
                effects.set(i, new ActiveAttributeEffect(entityId, attributeHolder, modifierId, expireTime));
                found = true;
                SpellElemental.LOGGER.debug("[AttributeEffectManager] Refreshed timed effect for entity {}, new expire time: {}", 
                    entityId, expireTime);
                break;
            }
        }
        
        // 如果没有找到相同修饰符，添加新的
        if (!found) {
            ActiveAttributeEffect effect = new ActiveAttributeEffect(entityId, attributeHolder, modifierId, expireTime);
            effects.add(effect);
            SpellElemental.LOGGER.debug("[AttributeEffectManager] Registered new timed effect for entity {}, expires at {}", 
                entityId, expireTime);
        }
    }
    
    /**
     * 服务器tick事件处理 - 检查并移除过期的属性效果
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_EFFECTS.isEmpty()) return;
        
        ServerLevel level = event.getServer().overworld(); // 获取主世界作为时间参考
        long currentTime = level.getGameTime();
        
        Iterator<Map.Entry<Integer, List<ActiveAttributeEffect>>> entityIterator = ACTIVE_EFFECTS.entrySet().iterator();
        
        while (entityIterator.hasNext()) {
            Map.Entry<Integer, List<ActiveAttributeEffect>> entry = entityIterator.next();
            int entityId = entry.getKey();
            List<ActiveAttributeEffect> effects = entry.getValue();
            
            // 获取实体
            net.minecraft.world.entity.Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                // 实体不存在或不是生物，移除所有效果
                entityIterator.remove();
                continue;
            }
            
            // 检查过期效果
            Iterator<ActiveAttributeEffect> effectIterator = effects.iterator();
            while (effectIterator.hasNext()) {
                ActiveAttributeEffect effect = effectIterator.next();
                
                if (currentTime >= effect.expireTime) {
                    // 效果过期，移除属性修饰符
                    removeAttributeModifier(livingEntity, effect.attributeHolder, effect.modifierId);
                    effectIterator.remove();
                    
                    SpellElemental.LOGGER.debug("[AttributeEffectManager] Removed expired effect for entity {}, modifier {}", 
                        entityId, effect.modifierId);
                }
            }
            
            // 如果该实体没有剩余效果，移除整个条目
            if (effects.isEmpty()) {
                entityIterator.remove();
            }
        }
    }
    
    /**
     * 移除属性修饰符
     */
    private static void removeAttributeModifier(LivingEntity entity,
                                              net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attributeHolder,
                                              ResourceLocation modifierId) {
        try {
            AttributeInstance instance = entity.getAttribute(attributeHolder);
            if (instance != null) {
                double valueBefore = instance.getValue();
                instance.removeModifier(modifierId);
                double valueAfter = instance.getValue();
                
                SpellElemental.LOGGER.info("[AttributeEffectManager] Removed modifier {} from {}, value: {} -> {}", 
                    modifierId, entity.getName().getString(), valueBefore, valueAfter);
            }
        } catch (Exception e) {
            SpellElemental.LOGGER.warn("[AttributeEffectManager] Failed to remove modifier {} from {}", 
                modifierId, entity.getName().getString(), e);
        }
    }
    
    /**
     * 手动移除实体的所有属性效果（用于实体死亡等情况）
     */
    public static void removeAllEffects(int entityId) {
        List<ActiveAttributeEffect> effects = ACTIVE_EFFECTS.remove(entityId);
        if (effects != null) {
            SpellElemental.LOGGER.debug("[AttributeEffectManager] Removed all effects for entity {}", entityId);
        }
    }
    
    /**
     * 获取活跃效果数量（用于调试）
     */
    public static int getActiveEffectCount() {
        return ACTIVE_EFFECTS.values().stream().mapToInt(List::size).sum();
    }
}
