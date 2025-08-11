package com.chadate.spellelemental.tick;

import com.chadate.spellelemental.element.attachment.environment.ElementsEnvironment;
import com.chadate.spellelemental.element.decay.ElementDecay;


import com.chadate.spellelemental.event.tick.TickEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class ApplayTickEventHandler {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        int entityId = entity.getId();

        ElementsEnvironment.applyWaterElementAttachment(entity);
        ElementDecay.processElements(entity, entityId);
//        CustomFreezeController.CheckFreezeStatus(event);

        TickEvent.VulnerabilityTick(event);
        TickEvent.CheckDewSparkLayer(event);
        TickEvent.FreezeResistanceDecay(event);
    }
}
