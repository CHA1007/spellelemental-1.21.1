package com.chadate.spellelemental.element.reaction;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ReactionEvent {
    @SubscribeEvent
    public static void applyElementAttachment(LivingDamageEvent.Pre event) {
        ReactionEventHandler.handleElementReactions(event);
    }
}
