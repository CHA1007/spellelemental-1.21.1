package com.chadate.spellelemental.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * 物理抗性下降粒子
 * 深紫色的破碎效果，表示物理防护被削弱
 */
@OnlyIn(Dist.CLIENT)
public class PhysicalResistanceDownParticle extends TextureSheetParticle {
    
    protected PhysicalResistanceDownParticle(ClientLevel level, double x, double y, double z, 
                                           double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        // 粒子基础属性
        this.lifetime = 40 + this.random.nextInt(20); // 2-3秒生命周期
        this.gravity = 0.075f; // 适中的重力，自然下落
        this.friction = 0.98f; // 空气阻力，减缓水平运动

        // 重新设置速度：主要向下，轻微水平飘动
        this.xd = (this.random.nextDouble() - 0.5) * 0.01; // 很小的水平速度
        this.yd = -0.01 - this.random.nextDouble() * 0.02; // 向下的初始速度
        this.zd = (this.random.nextDouble() - 0.5) * 0.01; // 很小的水平速度
    }

    @Override
    public void tick() {
        super.tick();

        // 添加轻微的左右摆动，模拟空气阻力
        if (this.age % 10 == 0) { // 每10tick调整一次
            this.xd += (this.random.nextDouble() - 0.5) * 0.002;
            this.zd += (this.random.nextDouble() - 0.5) * 0.002;
        }
        
        // 限制水平速度，防止飘得太远
        double maxHorizontalSpeed = 0.02;
        if (Math.abs(this.xd) > maxHorizontalSpeed) {
            this.xd = Math.signum(this.xd) * maxHorizontalSpeed;
        }
        if (Math.abs(this.zd) > maxHorizontalSpeed) {
            this.zd = Math.signum(this.zd) * maxHorizontalSpeed;
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /**
         * 粒子工厂
         */
        @OnlyIn(Dist.CLIENT)
        public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {

        @Override
            public Particle createParticle(@Nonnull SimpleParticleType type, @Nonnull ClientLevel level,
                                           double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed) {
                PhysicalResistanceDownParticle particle = new PhysicalResistanceDownParticle(
                        level, x, y, z, xSpeed, ySpeed, zSpeed);
                particle.pickSprite(this.sprites);
                return particle;
            }
        }
}
