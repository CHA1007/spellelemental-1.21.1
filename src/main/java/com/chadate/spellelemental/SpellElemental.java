package com.chadate.spellelemental;

import com.chadate.spellelemental.register.ModAttributes;
import com.chadate.spellelemental.command.DebugCommand;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.config.ClientConfig;
import com.chadate.spellelemental.element.attachment.attack.ElementEventHandler;
import com.chadate.spellelemental.element.attachment.data.UnifiedElementAttachmentDataRegistry;
import com.chadate.spellelemental.element.reaction.data.ElementReactionDataRegistry;
import com.chadate.spellelemental.element.reaction.runtime.AttributeEffectManager;
import com.chadate.spellelemental.element.reaction.runtime.ElementReactionHandler;
import com.chadate.spellelemental.event.crit.SpellDamageCritHandler;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import com.chadate.spellelemental.event.heal.HealingEventHandler;
import com.chadate.spellelemental.event.physical.PhysicalEventHandler;
import com.chadate.spellelemental.config.ServerConfig;
import com.chadate.spellelemental.register.ModSounds;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public SpellElemental(IEventBus modEventBus, ModContainer modContainer) {
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);
        ModSounds.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SpellDamageCritHandler::applyCritBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PhysicalEventHandler::applyPhysicalBonus);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ElementEventHandler::handleElementAttachment);
        NeoForge.EVENT_BUS.addListener(ElementReactionHandler::damageTypeReaction);
        NeoForge.EVENT_BUS.addListener(ElementReactionHandler::tickTypeReaction);
        NeoForge.EVENT_BUS.addListener(ElementReactionDataRegistry::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(UnifiedElementAttachmentDataRegistry::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(ElementEventHandler::onStartTracking);
        NeoForge.EVENT_BUS.addListener(HealingEventHandler::onLivingHeal);
        NeoForge.EVENT_BUS.addListener(ElementDecaySystem::elementDecay);
        NeoForge.EVENT_BUS.addListener(AttributeEffectManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(DebugCommand::onRegisterCommands);
        
        // 注册客户端设置事件监听器
        modEventBus.addListener(this::onClientSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", SpellElemental.MODID));
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, String.format("%s-server.toml", SpellElemental.MODID));

        // 刷新服务端配置覆盖缓存：在加载/重载时清空，确保游戏内修改立刻生效
        modEventBus.addListener((ModConfigEvent.Loading e) -> {
            if (e.getConfig().getSpec() == ServerConfig.SPEC) {
                ServerConfig.invalidateCache();
            }
        });
        modEventBus.addListener((ModConfigEvent.Reloading e) -> {
            if (e.getConfig().getSpec() == ServerConfig.SPEC) {
                ServerConfig.invalidateCache();
            }
        });
    }
    
    private void onClientSetup(FMLClientSetupEvent event) {
        // 委托给专门的客户端初始化类
        SpellElementalClient.init();
        
        // 执行客户端设置阶段的额外初始化
        event.enqueueWork(SpellElementalClient::setup);
    }
}