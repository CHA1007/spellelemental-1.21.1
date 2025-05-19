package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.custom.ElementsAttachment;
import com.chadate.spellelemental.element.reaction.ElementReactionManager;
import com.chadate.spellelemental.element.reaction.custom.fire.*;
import com.chadate.spellelemental.element.attachment.ElementAttachmentRegistry;
import com.chadate.spellelemental.element.attachment.custom.FireElementHandler;
import com.chadate.spellelemental.element.attachment.custom.IceElementHandler;
import com.chadate.spellelemental.element.attachment.custom.LightningElementHandler;
import com.chadate.spellelemental.element.attachment.custom.NatureElementHandler;
import com.chadate.spellelemental.element.reaction.custom.ice.IceMeltReaction;
import com.chadate.spellelemental.element.reaction.custom.ice.IceSuperconductiveReaction;
import com.chadate.spellelemental.element.reaction.custom.lightning.*;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureBurnReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureDewSparkReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureGerminateReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NaturePromotionReaction;
import com.chadate.spellelemental.event.DamageEvent;
import com.chadate.spellelemental.event.crit.CritEventHandler;
import com.chadate.spellelemental.event.custom.*;
import com.chadate.spellelemental.network.custom.ElementData;
import com.chadate.spellelemental.render.ElementRenderConfig;
import com.chadate.spellelemental.render.ElementRenderer;
import com.chadate.spellelemental.render.ElementRendererRegistry;
import com.chadate.spellelemental.render.custom.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import java.util.function.Supplier;
import java.util.*;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(SpellElemental.MODID)
public class SpellElemental {
    public static final String MODID = "spellelemental";

    private static final ElementRendererRegistry RENDERER_REGISTRY = new ElementRendererRegistry();
    private static final ElementReactionManager REACTION_MANAGER = new ElementReactionManager();

    public SpellElemental(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        ModAttributes.register(modEventBus);
        SpellAttachments.register(modEventBus);

        NeoForge.EVENT_BUS.register(CritEventHandler.class);
        NeoForge.EVENT_BUS.register(PhysicalEventHandler.class);

        ElementAttachmentRegistry.register(new FireElementHandler());
        ElementAttachmentRegistry.register(new IceElementHandler());
        ElementAttachmentRegistry.register(new LightningElementHandler());
        ElementAttachmentRegistry.register(new NatureElementHandler());

        //fire reaction
        REACTION_MANAGER.register(new FirePromotionReaction());
        REACTION_MANAGER.register(new FireCombustignitionReaction());
        REACTION_MANAGER.register(new FireDeflagrationReaction());
        REACTION_MANAGER.register(new FireFreezeMeltReaction());
        REACTION_MANAGER.register(new FireEvaporateReaction());
        REACTION_MANAGER.register(new FireMeltReaction());
        REACTION_MANAGER.register(new FireBurnReaction());

        //lightning reaction
        REACTION_MANAGER.register(new LightningSurgechargeReaction());
        REACTION_MANAGER.register(new LightningSurgeReaction());
        REACTION_MANAGER.register(new LightningDeflagrationReaction());
        REACTION_MANAGER.register(new LightningFreezeVulnerableReaction());
        REACTION_MANAGER.register(new LightningElectroReaction());
        REACTION_MANAGER.register(new LightningSuperconductiveReaction());
        REACTION_MANAGER.register(new LightningPromotionReaction());

        //ice reaction
        REACTION_MANAGER.register(new IceSuperconductiveReaction());
        REACTION_MANAGER.register(new IceMeltReaction());

        //nature reaction
        REACTION_MANAGER.register(new NatureGerminateReaction());
        REACTION_MANAGER.register(new NatureBurnReaction());
        REACTION_MANAGER.register(new NatureDewSparkReaction());
        REACTION_MANAGER.register(new NaturePromotionReaction());

        RENDERER_REGISTRY.register(new FireElementRenderer(0.12f, 0.9f));
        RENDERER_REGISTRY.register(new IceElementRenderer(0.12f, 0.9f));
        RENDERER_REGISTRY.register(new LightningElementRenderer(0.12f, 0.9f));
        RENDERER_REGISTRY.register(new NatureElementRenderer(0.12f, 0.9f));
        RENDERER_REGISTRY.register(new WaterElementRenderer(0.12f, 0.9f));
        RENDERER_REGISTRY.register(new FreezeElementRenderer(0.12f, 0.9f));

        NeoForge.EVENT_BUS.register(this);

    }


    @SubscribeEvent
    public void handleElementAttachment(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        int entityId = target.getId();
        System.out.println("1");
        ElementAttachmentRegistry.handleAttachment(target, source, entityId);
    }

    @SubscribeEvent
    public void handleElementReactions(LivingDamageEvent.Pre event) {
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        float astralBlessing = 0;

        if (attacker != null) {
            astralBlessing = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
        }

        if (DamageEvent.IsEntityDamage(event)) {
            DamageEvent.CancelSpellUnbeatableFrames(target);
        }

        REACTION_MANAGER.handleReaction(event, attacker, astralBlessing);
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        int entityId = entity.getId();
            TickEvent.BurnTick(event);
            if (entity.isInWaterOrRain()) {
                entity.getData(SpellAttachments.WATER_ELEMENT).setValue(200);
            }
            processElement(entity, entityId, SpellAttachments.FIRE_ELEMENT, "fire_element");
            processElement(entity, entityId, SpellAttachments.ICE_ELEMENT, "ice_element");
            processElement(entity, entityId, SpellAttachments.LIGHTNING_ELEMENT, "lightning_element");
            processElement(entity, entityId, SpellAttachments.WATER_ELEMENT, "water_element");
            processElement(entity, entityId, SpellAttachments.NATURE_ELEMENT, "nature_element");
            processElement(entity, entityId, SpellAttachments.PROMOTION_ELEMENT, "promotion_element");

        if (event.getEntity().tickCount % 10 == 0) {
            TickEvent.ElectroReaction(event);
            TickEvent.VulnerabilityTick(event);
            TickEvent.CheckFreezeStatus(event);
            TickEvent.FreezeElementTick(event);
            TickEvent.CheckDewSparkLayer(event);
        }

        if(event.getEntity().tickCount % 200 == 0){
            TickEvent.FreezeResistanceDecay(event);
        }
    }

    private final Map<String, Boolean> elementZeroStateCache = new HashMap<>();

    private static final String CACHE_KEY_SEPARATOR = "|";

    private void processElement(Entity entity, int entityId,
                                Supplier<AttachmentType<ElementsAttachment>> elementKey,
                                String elementName) {

        if (entity == null || elementKey == null || elementName == null || elementName.isBlank()) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        AttachmentType<ElementsAttachment> type = elementKey.get();
        if (type == null || !entity.hasData(type)) {
            return;
        }

        ElementsAttachment attachment = entity.getData(type);
        int duration = attachment.getValue();
        String cacheKey = entityId +
                CACHE_KEY_SEPARATOR +
                elementName;

        if (duration > 0) {
            entity.setData(type, new ElementsAttachment(duration - 1));
            elementZeroStateCache.remove(cacheKey);
        } else {

            if (attachment.getValue() != 0) {
                entity.setData(type, new ElementsAttachment(0));
            }

            if (elementZeroStateCache.putIfAbsent(cacheKey, Boolean.TRUE) == null) {
                PacketDistributor.sendToAllPlayers(new ElementData(entityId, elementName, 0));
            }
        }
    }

    private float calculateAlpha(ElementRenderer renderer, LivingEntity entity) {
        // 获取元素剩余时间
        int duration = getElementDuration(entity, renderer);

        // 防御性检查
        if (entity == null || duration <= 0) {
            return 0f;
        }

        // 基础透明度
        float baseAlpha = renderer.getBaseAlpha();

        // 分阶段处理透明度
        if (duration > 100) {
            return baseAlpha; // 前100tick保持稳定
        } else if (duration > 50) {
            // 中间阶段线性衰减（100 -> 50）
            return baseAlpha * (duration / 100f);
        } else{
            // 最后50tick闪烁效果
            return calculateFlickerAlpha(duration, baseAlpha);
        }
    }

    // 使用正弦波生成闪烁效果
    private float calculateFlickerAlpha(int duration, float baseAlpha) {
        // 基于tick计数生成动态波动
        double flickerSpeed = 0.25; // 闪烁速度系数
        double wave = Math.sin(System.currentTimeMillis() * flickerSpeed * 0.01 * duration);

        // 波动范围：0.4 - 0.8 * baseAlpha
        return (float) (baseAlpha * (0.6 + 0.4 * wave));
    }

    private int getElementDuration(LivingEntity entity, ElementRenderer renderer) {
        // 空值保护
        if (entity == null || renderer == null) {
            return 0;
        }

        // 获取附件类型
        AttachmentType<ElementsAttachment> type = getAttachmentType(renderer);
        if (type == null) {
            return 0;
        }

        // 安全访问数据
        if (entity.hasData(type)) {
            return entity.getData(type).getValue();
        }

        return 0;
    }

    private AttachmentType<ElementsAttachment> getAttachmentType(ElementRenderer renderer) {
        String path = renderer.getTexture().getPath();

        if (path.contains("fire")) return SpellAttachments.FIRE_ELEMENT.get();
        if (path.contains("water")) return SpellAttachments.WATER_ELEMENT.get();
        if (path.contains("ice")) return SpellAttachments.ICE_ELEMENT.get();
        if (path.contains("lightning")) return SpellAttachments.LIGHTNING_ELEMENT.get();
        if (path.contains("nature")) return SpellAttachments.NATURE_ELEMENT.get();
        if (path.contains("freeze")) return SpellAttachments.FREEZE_ELEMENT.get();

        return null;
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        ElementRenderConfig config = getConfig();
        Minecraft mc = Minecraft.getInstance();

        if (!shouldRender(event, mc, config)) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();

        List<ElementRenderer> activeRenderers = ElementRendererRegistry.getActiveRenderers(entity);

        renderElements(
                poseStack,
                bufferSource,
                packedLight,
                activeRenderers,
                entity.getBbHeight() + config.heightOffset,
                mc.gameRenderer.getMainCamera().getYRot(),
                config,
                entity
        );
    }

    private ElementRenderConfig getConfig() {
        return ElementRenderConfig.getInstance();
    }
    private void renderElements(PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, List<ElementRenderer> renderers,
                                float height, float cameraYaw, ElementRenderConfig config, LivingEntity entity) {
        if (renderers.isEmpty()) return;

        poseStack.pushPose();
        try {
            poseStack.translate(0, height, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYaw));

            float spacing = config.spacing;
            float startX = -(renderers.size() - 1) * spacing / 2f;

            for (int i = 0; i < renderers.size(); i++) {
                ElementRenderer renderer = renderers.get(i);
                float xOffset = startX + i * spacing;

                renderElement(
                        poseStack,
                        bufferSource,
                        packedLight,
                        renderer,
                        xOffset,
                        config,
                        entity
                );
            }
        } finally {
            poseStack.popPose();
        }
    }

    private boolean shouldRender(RenderLivingEvent.Post<? extends LivingEntity, ?> event, Minecraft mc, ElementRenderConfig config) {
        Entity cameraEntity = mc.getCameraEntity();
        return cameraEntity != null
                && event.getEntity() != cameraEntity
                && event.getEntity().distanceToSqr(cameraEntity) <= config.maxDistanceSq;
    }

    private void renderElement(PoseStack poseStack, MultiBufferSource bufferSource,
                               int packedLight, ElementRenderer renderer,
                               float xOffset, ElementRenderConfig config, LivingEntity entity) {
        poseStack.pushPose();
        try {
            poseStack.translate(xOffset, 0, 0);
            poseStack.scale(renderer.getScale(), renderer.getScale(), renderer.getScale());

            // 使用传入的entity参数而非渲染器内部引用
            float alpha = calculateAlpha(renderer, entity); // entity来自外部参数

            renderer.render(
                    entity, // 直接传递实体
                    poseStack,
                    bufferSource,
                    packedLight,
                    alpha
            );
        } finally {
            poseStack.popPose();
        }
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        elementZeroStateCache.clear();
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

}