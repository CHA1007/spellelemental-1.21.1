package com.chadate.spellelemental.tick;

import net.neoforged.bus.api.SubscribeEvent;

public class ApplayTickEvent {
    @SubscribeEvent
    public static void applyEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
        ApplayTickEventHandler.onEntityTick(event);
    }
}
