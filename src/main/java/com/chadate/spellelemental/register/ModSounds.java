package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, SpellElemental.MODID);

    // 法术暴击音效
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_CRIT = SOUND_EVENTS.register(
            "spell_crit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "spell_crit"))
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
