package com.chadate.spellelemental.render.element;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.render.element.custom.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RendererEventHandler {
    private static final int STABLE_DURATION = 100;
    private static final int FADE_DURATION = 50;
    private static final double FLICKER_SPEED = 0.25;
    public static void initialize(IEventBus eventBus, ElementRendererRegistry registry) {
        // 注册事件监听器
        eventBus.addListener(EventPriority.NORMAL, RendererEventHandler::handleElementRenderer);

        // 注册渲染器
        registerRenderers(registry);
    }

    private static final Map<String, RendererConfig> RENDERER_CONFIGS = Map.of(
            "fire", new RendererConfig(0.12f, 0.9f),
            "ice", new RendererConfig(0.12f, 0.9f),
            "lightning", new RendererConfig(0.12f, 0.9f),
            "nature", new RendererConfig(0.12f, 0.9f),
            "water", new RendererConfig(0.12f, 0.9f),
            "freeze", new RendererConfig(0.12f, 0.9f)
    );

    // 使用LinkedHashMap保持插入顺序
    private static final Map<String, RendererFactory> RENDERER_FACTORIES = Map.of(
            "fire", FireElementRenderer::new,
            "ice", IceElementRenderer::new,
            "lightning", LightningElementRenderer::new,
            "nature", NatureElementRenderer::new,
            "water", WaterElementRenderer::new,
            "freeze", FreezeElementRenderer::new
    );

    private static final Map<String, Supplier<AttachmentType<ElementsAttachment>>> ATTACHMENT_MAPPING = Map.of(
            "fire", SpellAttachments.FIRE_ELEMENT,
            "water", SpellAttachments.WATER_ELEMENT,
            "ice", SpellAttachments.ICE_ELEMENT,
            "lightning", SpellAttachments.LIGHTNING_ELEMENT,
            "nature", SpellAttachments.NATURE_ELEMENT,
            "freeze", SpellAttachments.FREEZE_ELEMENT
    );
    public static void registerRenderers(ElementRendererRegistry registry) {
        RENDERER_CONFIGS.forEach((type, config) -> {
            RendererFactory factory = RENDERER_FACTORIES.get(type);
            if (factory != null) {
                registry.register(factory.create(config.scale(), config.alpha()));
            }
        });
    }

    private record RendererConfig(float scale, float alpha) {}
    @SubscribeEvent
    public static void handleElementRenderer(RenderLivingEvent.Post<LivingEntity, ?> event) {
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

    private static ElementRenderConfig getConfig() {
        return ElementRenderConfig.getInstance();
    }
    private static void renderElements(PoseStack poseStack, MultiBufferSource bufferSource,
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

    private static boolean shouldRender(RenderLivingEvent.Post<? extends LivingEntity, ?> event, Minecraft mc, ElementRenderConfig config) {
        Entity cameraEntity = mc.getCameraEntity();
        return cameraEntity != null
                && event.getEntity() != cameraEntity
                && event.getEntity().distanceToSqr(cameraEntity) <= config.maxDistanceSq;
    }

    private static void renderElement(PoseStack poseStack, MultiBufferSource bufferSource,
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
    private static float calculateAlpha(ElementRenderer renderer, LivingEntity entity) {
        int duration = getElementDuration(entity, renderer);
        if (entity == null || duration <= 0) return 0f;

        float baseAlpha = renderer.getBaseAlpha();

        if (duration > STABLE_DURATION) {
            return baseAlpha;
        } else if (duration > FADE_DURATION) {
            return baseAlpha * (duration / (float)STABLE_DURATION);
        } else {
            return calculateFlickerAlpha(duration, baseAlpha);
        }
    }

    private static int getElementDuration(LivingEntity entity, ElementRenderer renderer) {
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

    private static float calculateFlickerAlpha(int duration, float baseAlpha) {
        long gameTime = 0;
        if (Minecraft.getInstance().level != null) {
            gameTime = Minecraft.getInstance().level.getGameTime();
        }
        double wave = Math.sin(gameTime * FLICKER_SPEED * 0.01 * duration);
        return (float) (baseAlpha * (0.6 + 0.4 * wave));
    }

    private static AttachmentType<ElementsAttachment> getAttachmentType(ElementRenderer renderer) {
        String type = ATTACHMENT_MAPPING.keySet().stream()
                .filter(k -> renderer.getTexture().getPath().contains(k))
                .findFirst()
                .orElse(null);

        return type != null ? ATTACHMENT_MAPPING.get(type).get() : null;
    }

    @FunctionalInterface
    private interface RendererFactory {
        ElementRenderer create(float scale, float alpha);
    }
}
