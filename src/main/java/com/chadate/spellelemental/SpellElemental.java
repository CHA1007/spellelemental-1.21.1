package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.ElementEventHandler;
import com.chadate.spellelemental.element.attachment.attack.ElementHandlerRegistrar;
import com.chadate.spellelemental.element.attachment.attack.custom.BloodElementHandler;
import com.chadate.spellelemental.element.attachment.attack.custom.HolyElementHander;
import com.chadate.spellelemental.element.reaction.custom.ReactionEventHandler;
import com.chadate.spellelemental.event.crit.CritEventHandler;
import com.chadate.spellelemental.event.effect.*;
import com.chadate.spellelemental.event.heal.HealingEventHandler;
import com.chadate.spellelemental.event.physical.*;
import com.chadate.spellelemental.cilent.render.element.icon.ElementRendererRegistry;
import com.chadate.spellelemental.cilent.render.damage.DamageNumberRenderer;
import com.chadate.spellelemental.cilent.render.element.icon.RendererEventHandler;
import com.chadate.spellelemental.tick.ApplayTickEventHandler;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    private static final ElementRendererRegistry RENDERER_REGISTRY = new ElementRendererRegistry();

    public SpellElemental(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CritEventHandler::applyCritBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PhysicalEventHandler::applyPhysicalBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ElementEventHandler::handleElementAttachment);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, ReactionEventHandler::handleElementReactions);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, ApplayTickEventHandler::onEntityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, DamageNumberRenderer::onRenderLevelStage);
        ElementHandlerRegistrar.registerDefaultHandlers();
        RendererEventHandler.initialize(NeoForge.EVENT_BUS, RENDERER_REGISTRY);
        NeoForge.EVENT_BUS.register(HealingEventHandler.class);
        NeoForge.EVENT_BUS.register(LightningAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(FireAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(IceAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(WaterAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(BloodAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(EnderAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(HolyAuraEventHandler.class);
        NeoForge.EVENT_BUS.register(NatureAuraEventHandler.class);
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}