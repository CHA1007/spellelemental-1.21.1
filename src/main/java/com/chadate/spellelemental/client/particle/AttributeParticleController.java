package com.chadate.spellelemental.client.particle;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.register.ModAttributes;
import com.chadate.spellelemental.register.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.*;

/**
 * 属性效果控制器
 * 监控实体属性修饰符变化，自动播放对应的粒子效果
 */
@EventBusSubscriber(modid = SpellElemental.MODID, value = Dist.CLIENT)
public class AttributeParticleController {
    
    // 追踪实体的物理抗性修饰符状态
    private static final Map<UUID, Set<ResourceLocation>> TRACKED_PHYSICAL_RESIST_MODIFIERS = new HashMap<>();
    
    // 粒子生成计时器
    private static final Map<UUID, Integer> PARTICLE_TIMERS = new HashMap<>();
    
    // 粒子生成间隔（tick）
    private static final int PARTICLE_INTERVAL = 10;
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        
        ClientLevel level = mc.level;
        if (level == null) return; // 额外的空指针检查
        
        // 检查所有生物实体的属性修饰符
        level.entitiesForRendering().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                checkPhysicalResistanceModifiers(livingEntity);
            }
        });
        
        // 清理已移除实体的数据
        cleanupRemovedEntities(level);
    }
    
    /**
     * 检查实体的物理抗性修饰符变化
     */
    private static void checkPhysicalResistanceModifiers(LivingEntity entity) {
        UUID entityId = entity.getUUID();
        AttributeInstance physicalResistInstance = entity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
        
        if (physicalResistInstance == null) return;
        
        // 检查是否有负面修饰符（降低物理抗性的）
        boolean hasNegativeModifier = false;
        for (AttributeModifier modifier : physicalResistInstance.getModifiers()) {
            // 检查是否为负面修饰符（降低抗性）
            if (modifier.amount() < 0) {
                hasNegativeModifier = true;
                break;
            }
        }
        
        // 如果有负面修饰符存在，播放粒子效果
        if (hasNegativeModifier) {
            spawnPhysicalResistanceDownParticles(entity);
            TRACKED_PHYSICAL_RESIST_MODIFIERS.put(entityId, new HashSet<>());
        } else {
            // 移除粒子计时器和追踪状态
            PARTICLE_TIMERS.remove(entityId);
            TRACKED_PHYSICAL_RESIST_MODIFIERS.remove(entityId);
        }
    }
    
    /**
     * 在实体周围生成物理抗性下降粒子
     */
    private static void spawnPhysicalResistanceDownParticles(LivingEntity entity) {
        UUID entityId = entity.getUUID();
        int timer = PARTICLE_TIMERS.getOrDefault(entityId, 0);
        
        // 控制粒子生成频率
        if (timer <= 0) {
            ClientLevel level = (ClientLevel) entity.level();
            
            // 在实体周围生成1-3个粒子
            int particleCount = 1 + level.random.nextInt(3);
            for (int i = 0; i < particleCount; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double radius = 0.3 + level.random.nextDouble() * 0.4; // 0.3-0.7半径
                double height = level.random.nextDouble() * entity.getBbHeight();
                
                double x = entity.getX() + Math.cos(angle) * radius;
                double y = entity.getY() + height;
                double z = entity.getZ() + Math.sin(angle) * radius;
                
                // 生成粒子
                level.addParticle(
                    ModParticles.PHYSICAL_RESISTANCE_DOWN.get(),
                    x, y, z,
                    0, 0, 0 // 速度由粒子类内部控制
                );
            }
            
            // 重置计时器
            PARTICLE_TIMERS.put(entityId, PARTICLE_INTERVAL);
        } else {
            PARTICLE_TIMERS.put(entityId, timer - 1);
        }
    }
    
    /**
     * 清理已移除实体的数据
     */
    private static void cleanupRemovedEntities(ClientLevel level) {
        Set<UUID> existingEntities = new HashSet<>();
        level.entitiesForRendering().forEach(entity -> {
            if (entity instanceof LivingEntity) {
                existingEntities.add(entity.getUUID());
            }
        });
        
        // 移除不存在的实体数据
        TRACKED_PHYSICAL_RESIST_MODIFIERS.keySet().removeIf(uuid -> !existingEntities.contains(uuid));
        PARTICLE_TIMERS.keySet().removeIf(uuid -> !existingEntities.contains(uuid));
    }
    
    /**
     * 手动清理指定实体的数据（用于实体移除时）
     */
    public static void cleanupEntity(UUID entityId) {
        TRACKED_PHYSICAL_RESIST_MODIFIERS.remove(entityId);
        PARTICLE_TIMERS.remove(entityId);
    }
}
