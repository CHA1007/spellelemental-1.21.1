package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.ElementEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chadate.spellelemental.event.crit.CritEventHandler;
import com.chadate.spellelemental.event.effect.*;
import com.chadate.spellelemental.event.heal.HealingEventHandler;
import com.chadate.spellelemental.event.physical.*;
import com.chadate.spellelemental.client.render.element.icon.ElementRendererRegistry;
import com.chadate.spellelemental.client.render.damage.DamageNumberRenderer;
import com.chadate.spellelemental.client.render.element.icon.RendererEventHandler;
import com.chadate.spellelemental.tick.ApplayTickEventHandler;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    public static final Logger LOGGER = LoggerFactory.getLogger(SpellElemental.class);
    private static final ElementRendererRegistry RENDERER_REGISTRY = new ElementRendererRegistry();

    public SpellElemental(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CritEventHandler::applyCritBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PhysicalEventHandler::applyPhysicalBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ElementEventHandler::handleElementAttachment);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, ApplayTickEventHandler::onEntityTick);

        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, DamageNumberRenderer::onRenderLevelStage);
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
        event.enqueueWork(() -> {
            // 统一元素附着数据加载器（包含伤害源和环境条件）
            // 元素反应数据加载器
            // 环境条件数据加载器
            // 注意：在 NeoForge 中，数据加载器通过 @EventBusSubscriber 注解自动注册
            // UnifiedElementAttachmentDataRegistry 和 ElementReactionDataRegistry 会自动处理数据包重载
            LOGGER.info("SpellElemental 模组初始化完成 - 统一数据包驱动架构");
            LOGGER.info("SpellElemental common setup completed - Data pack driven systems initialized");
        });
    }
}