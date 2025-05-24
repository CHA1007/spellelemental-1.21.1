package com.chadate.spellelemental.cilent.render.element.icon;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class RendererEvent {
    @SubscribeEvent
    public static void applyElementRenderer(RenderLivingEvent.Post<LivingEntity, ?> event) {
        RendererEventHandler.handleElementRenderer(event);
    }
}
