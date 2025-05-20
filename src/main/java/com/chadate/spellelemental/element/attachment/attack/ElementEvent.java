package com.chadate.spellelemental.element.attachment.attack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ElementEvent {
    @SubscribeEvent
    public static void applyElementAttachment(LivingDamageEvent.Pre event) {
        ElementEventHandler.handleElementAttachment(event);
    }
}
