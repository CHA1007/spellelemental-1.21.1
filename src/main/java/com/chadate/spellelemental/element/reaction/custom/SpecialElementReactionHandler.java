package com.chadate.spellelemental.element.reaction.custom;

import com.chadate.spellelemental.element.reaction.special.Burning;
import com.chadate.spellelemental.element.reaction.special.ElectroCharged;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

public class SpecialElementReactionHandler {
    @SubscribeEvent
    public static void handleEntityTick(EntityTickEvent.Pre event) {
        List<SpecialElementReaction> reactions = new ArrayList<>();
        reactions.add(new Burning());
        reactions.add(new ElectroCharged());

        for (SpecialElementReaction reaction : reactions) {
            if (reaction.appliesTo(event)) {
                reaction.apply(event);
            }
        }
    }
}
