package com.chadate.spellelemental.util;

import com.chadate.spellelemental.register.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 粒子测试工具类
 * 用于测试和调试粒子效果
 */
public class ParticleTestUtil {

    /**
     * 在实体周围生成物理抗性下降粒子
     * @param entity 目标实体
     * @param count 粒子数量
     */
    public static void spawnPhysicalResistanceDownParticles(LivingEntity entity, int count) {
        Level level = entity.level();
        if (level instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();
            
            // 在实体周围生成粒子
            for (int i = 0; i < count; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
                double offsetY = level.random.nextDouble() * entity.getBbHeight();
                double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
                
                double velocityX = (level.random.nextDouble() - 0.5) * 0.1;
                double velocityY = -0.05 - level.random.nextDouble() * 0.05; // 向下飘落
                double velocityZ = (level.random.nextDouble() - 0.5) * 0.1;
                
                serverLevel.sendParticles(
                    ModParticles.PHYSICAL_RESISTANCE_DOWN.get(),
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, // 粒子数量
                    0, 0, 0, // 额外的随机偏移
                    0.0 // 速度
                );
            }
        }
    }

    /**
     * 在指定位置生成物理抗性下降粒子爆发效果
     * @param level 世界
     * @param x X坐标
     * @param y Y坐标  
     * @param z Z坐标
     * @param intensity 强度（粒子数量倍数）
     */
    public static void spawnPhysicalResistanceDownBurst(Level level, double x, double y, double z, int intensity) {
        if (level instanceof ServerLevel serverLevel) {
            int particleCount = 10 * intensity;
            
            for (int i = 0; i < particleCount; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double radius = level.random.nextDouble() * 1.5;
                double height = level.random.nextDouble() * 2.0 - 1.0;
                
                double particleX = x + Math.cos(angle) * radius;
                double particleY = y + height;
                double particleZ = z + Math.sin(angle) * radius;
                
                double velocityX = Math.cos(angle) * 0.1;
                double velocityY = -0.1 + level.random.nextDouble() * 0.05;
                double velocityZ = Math.sin(angle) * 0.1;
                
                serverLevel.sendParticles(
                    ModParticles.PHYSICAL_RESISTANCE_DOWN.get(),
                    particleX, particleY, particleZ,
                    1,
                    velocityX, velocityY, velocityZ,
                    0.1
                );
            }
        }
    }
}
