package com.chadate.spellelemental.client.render;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.client.network.custom.ClientPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = SpellElemental.MODID, value = Dist.CLIENT)
public class ElementIconRenderer {
	@SubscribeEvent
	public static void onRenderLivingPost(RenderLivingEvent.Post<LivingEntity, ?> event) {
		LivingEntity entity = event.getEntity();
		var container = entity.getData(com.chadate.spellelemental.data.SpellAttachments.ELEMENTS_CONTAINER);
		Map<String, Integer> snapshot = container.snapshot();
		if (snapshot.isEmpty()) return;

		ElementIconRenderConfig cfg = ElementIconRenderConfig.get();
		List<String> elementKeys = new ArrayList<>(snapshot.keySet());
		int count = elementKeys.size();

		var poseStack = event.getPoseStack();
		MultiBufferSource buffer = event.getMultiBufferSource();
		poseStack.pushPose();
		poseStack.translate(0.0D, entity.getBbHeight() + cfg.getVerticalOffset(), 0.0D);

		if (cfg.isFaceCamera()) {
			poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
			if (cfg.isFlipBillboard()) poseStack.scale(-1.0F, -1.0F, 1.0F);
		}

		double totalWidth = (count - 1) * cfg.getHorizontalSpacing();
		double startX = -totalWidth / 2.0D;

		for (int i = 0; i < count; i++) {
			String elementKey = elementKeys.get(i);
			String iconPath = com.chadate.spellelemental.element.attachment.data.UnifiedElementAttachmentAssets.getIcon(elementKey);
			if (iconPath == null) continue;

			poseStack.pushPose();
			poseStack.translate(startX + i * cfg.getHorizontalSpacing(), 0.0D, 0.0D);
			poseStack.scale(cfg.getQuadScale(), cfg.getQuadScale(), cfg.getQuadScale());

			ResourceLocation rl = resolveTexture(iconPath);
			if (rl != null) {
				var matrix = poseStack.last().pose();
				var vc = buffer.getBuffer(RenderType.entityTranslucent(rl));
				float z = cfg.getZDepth();

				int remain = ClientPayloadHandler.DisplayCache.predictRemaining(entity.getId(), elementKey);
				float a = computeAlpha(remain, cfg);
				if (a <= 0f) { poseStack.popPose(); continue; }

				float[][] vertices = {
						{-1, 1, 0, 0},
						{1, 1, 1, 0},
						{1, -1, 1, 1},
						{-1, -1, 0, 1}
				};
				for (float[] v : vertices) {
					vc.addVertex(matrix, v[0], v[1], z)
							.setColor(1, 1, 1, a)
							.setUv(v[2], v[3])
							.setOverlay(OverlayTexture.NO_OVERLAY)
							.setLight(event.getPackedLight())
							.setNormal(0, 1, 0);
				}
			}

			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private static float computeAlpha(int remain, ElementIconRenderConfig cfg) {
		if (remain < 0) return cfg.getAlpha();
		int th = cfg.getFlashingThreshold();
		if (remain > th) return cfg.getAlpha();
		// 线性缩放透明度: remain=th -> maxAlpha, remain->0 -> minAlpha
		float t = Math.max(0f, Math.min(1f, remain / (float) th));
		float baseAlpha = cfg.getMinAlpha() + (cfg.getMaxAlpha() - cfg.getMinAlpha()) * t;
		// 闪烁频率随剩余降低而升高
		float hz = cfg.getMinFlashHz() + (cfg.getMaxFlashHz() - cfg.getMinFlashHz()) * (1f - t);
		float timeSec = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) / 20f;
		float phase = (float)Math.sin(2 * Math.PI * hz * timeSec);
		float flicker = 0.5f * (phase * 0.5f + 0.5f); // 0..0.5 之间微变
		return Math.max(0f, Math.min(1f, baseAlpha - flicker));
	}

	private static ResourceLocation resolveTexture(String iconPath) {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (iconPath != null && !iconPath.isEmpty()) {
				ResourceLocation rl = ResourceLocation.parse(iconPath);
				Optional<Resource> res = mc.getResourceManager().getResource(rl);
				if (res.isPresent()) return rl;
				String path = rl.getPath();
				String candidate = path;
				if (candidate.startsWith("textures/")) candidate = candidate.substring("textures/".length());
				if (candidate.endsWith(".png")) candidate = candidate.substring(0, candidate.length() - 4);
				ResourceLocation rl2 = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "textures/" + candidate + ".png");
				if (mc.getResourceManager().getResource(rl2).isPresent()) return rl2;
			}
		} catch (Exception ignored) {}
		return null;
	}
} 