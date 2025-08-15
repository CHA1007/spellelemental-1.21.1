package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.ElementEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chadate.spellelemental.event.crit.CritEventHandler;
import com.chadate.spellelemental.event.heal.HealingEventHandler;
import com.chadate.spellelemental.event.physical.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.chadate.spellelemental.command.DebugCommand;
import com.chadate.spellelemental.event.element.ElementEnvironmentSystem;


@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public SpellElemental(IEventBus modEventBus) {
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);



        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CritEventHandler::applyCritBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PhysicalEventHandler::applyPhysicalBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ElementEventHandler::handleElementAttachment);
        NeoForge.EVENT_BUS.register(HealingEventHandler.class);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, ElementDecaySystem::onServerTick);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, ElementEnvironmentSystem::onServerTick);
        NeoForge.EVENT_BUS.addListener(SpellElemental::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());
    }
}