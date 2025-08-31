package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 粒子类型注册器
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = 
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, SpellElemental.MODID);

    /**
     * 物理抗性下降粒子
     * 表现为深紫色的破碎效果粒子，向下飘落
     */
    public static final Supplier<SimpleParticleType> PHYSICAL_RESISTANCE_DOWN = 
        PARTICLE_TYPES.register("physical_resistance_down", () -> new SimpleParticleType(false));

    /**
     * 注册粒子类型到模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
        SpellElemental.LOGGER.info("已注册粒子类型");
    }
}
