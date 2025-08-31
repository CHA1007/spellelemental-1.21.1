package com.chadate.spellelemental.client.particle;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.register.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * 客户端粒子提供器注册
 */
@EventBusSubscriber(modid = SpellElemental.MODID, value = Dist.CLIENT)
public class ModParticleProviders {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        // 注册物理抗性下降粒子提供器
        event.registerSpriteSet(ModParticles.PHYSICAL_RESISTANCE_DOWN.get(), 
            PhysicalResistanceDownParticle.Provider::new);
        
    }
}
