package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.command.DebugCommand;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.ElementEventHandler;
import com.chadate.spellelemental.event.crit.CritEventHandler;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import com.chadate.spellelemental.event.element.DotSystem;
import com.chadate.spellelemental.event.heal.HealingEventHandler;
import com.chadate.spellelemental.event.physical.PhysicalEventHandler;
import com.chadate.spellelemental.sound.ModSounds;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public SpellElemental(IEventBus modEventBus) {
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);
        ModSounds.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CritEventHandler::applyCritBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PhysicalEventHandler::applyPhysicalBonus);
        NeoForge.EVENT_BUS.addListener(ElementEventHandler::handleElementAttachment);
        NeoForge.EVENT_BUS.addListener(ElementEventHandler::handleEnvironmentalAttachment);
        NeoForge.EVENT_BUS.addListener(ElementEventHandler::onStartTracking);
        NeoForge.EVENT_BUS.addListener(HealingEventHandler::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(ElementDecaySystem::onServerTick);
        NeoForge.EVENT_BUS.addListener(DotSystem::onServerTick);
        NeoForge.EVENT_BUS.addListener(DebugCommand::onRegisterCommands);
    }

}