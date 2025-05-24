package com.chadate.spellelemental.event.effect.effect;

import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.Random;

public class WaterAuraEffect {
    private static final int PARTICLE_COUNT = 2; // 每tick只生成2个粒子
    private static final Random random = new Random();

    public static void applyWaterAura(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        
        // 检查实体是否有水元素附着
        if (entity.getData(SpellAttachments.WATER_ELEMENT).getValue() <= 0) return;

        // 获取实体的高度
        double entityHeight = entity.getBbHeight();
        
        // 生成简单的点缀粒子
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // 随机生成位置
            double angle = random.nextDouble() * 2 * Math.PI;
            double height = random.nextDouble() * entityHeight;
            double radius = 0.3; // 贴近实体表面
            
            // 计算粒子位置
            double x = radius * Math.cos(angle);
            double y = height;
            double z = radius * Math.sin(angle);
            
            // 计算粒子位置（相对于实体）
            double particleX = entity.getX() + x;
            double particleY = entity.getY() + y;
            double particleZ = entity.getZ() + z;
            
            // 生成水滴粒子
            entity.level().addParticle(
                ParticleTypes.DRIPPING_WATER,
                particleX, particleY, particleZ,
                0, -0.05, 0 // 给一个很小的向下速度
            );
        }
    }
} 