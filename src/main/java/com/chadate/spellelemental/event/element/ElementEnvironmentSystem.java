package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;
import com.chadate.spellelemental.element.attachment.data.EnvironmentalAttachmentRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class ElementEnvironmentSystem {
	private ElementEnvironmentSystem() {}

	public static void onServerTick(ServerTickEvent.Post event) {
		ServerLevel level = event.getServer().overworld();
		if (level == null) return;
		List<UnifiedElementAttachmentConfig> configs = EnvironmentalAttachmentRegistry.getAll();
		if (configs.isEmpty()) return;

		for (ServerLevel srv : event.getServer().getAllLevels()) {
			srv.getEntities().getAll().forEach(entity -> {
				if (!(entity instanceof LivingEntity living)) return;
				for (UnifiedElementAttachmentConfig cfg : configs) {
					UnifiedElementAttachmentConfig.EnvironmentalConditions env = cfg.getEnvironmentalConditions();
					if (env == null || env.getWaterConditions() == null) continue;
					int interval = Math.max(1, env.getCheckInterval());
					if (living.tickCount % interval != 0) continue;
					boolean needInWater = env.getWaterConditions().isInWater();
					boolean needInRain = env.getWaterConditions().isInRain();
					boolean ok = false;
					if (needInWater && living.isInWaterOrBubble()) ok = true;
					if (needInRain && living.level().isRainingAt(living.blockPosition())) ok = true;
					if (!needInWater && !needInRain) ok = false;
					if (!ok) continue;
					applyAttachment(living, cfg);
				}
			});
		}
	}

	private static void applyAttachment(LivingEntity entity, UnifiedElementAttachmentConfig cfg) {
		ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
		int duration = cfg.getEffects() != null ? cfg.getEffects().getDuration() : 200;
		String elementKey = extractElementKey(cfg.getAttachmentType());
		container.setValue(elementKey, duration);
		// 跟踪衰减，离开环境后由衰减系统自然清除
		ElementDecaySystem.track(entity);
		PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), elementKey, duration));
	}

	private static String extractElementKey(String attachmentTypeName) {
		if (attachmentTypeName == null) return "";
		String s = attachmentTypeName.contains(":" ) ? attachmentTypeName.substring(attachmentTypeName.indexOf(":" ) + 1) : attachmentTypeName;
		return s.toLowerCase();
	}
} 