package com.chadate.spellelemental.element.attachment.environmental;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 环境条件元素附着事件处理器
 * 监听实体tick事件，处理环境条件检查
 */
public class EnvironmentalEventHandler {
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        // 只处理生物实体
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        
        // 处理环境条件检查
        EnvironmentalAttachmentRegistry.handleEnvironmentalCheck(livingEntity, livingEntity.tickCount);
    }
}
