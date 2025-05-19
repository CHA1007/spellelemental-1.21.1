package com.chadate.spellelemental;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class RendererEvent {
    @SubscribeEvent
    public static void applyElementRenderer(RenderLivingEvent.Post<LivingEntity, ?> event) {
        RendererEventHandler.handleElementRenderer(event);
    }
}
