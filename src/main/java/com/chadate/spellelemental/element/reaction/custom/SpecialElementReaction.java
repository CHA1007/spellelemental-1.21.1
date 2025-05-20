package com.chadate.spellelemental.element.reaction.custom;

import net.neoforged.neoforge.event.tick.EntityTickEvent;

public interface SpecialElementReaction {
    boolean appliesTo(EntityTickEvent.Pre event);
    void apply(EntityTickEvent.Pre event);
}
