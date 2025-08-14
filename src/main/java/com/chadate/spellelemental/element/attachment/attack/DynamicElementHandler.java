package com.chadate.spellelemental.element.attachment.attack;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;
import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.regex.Pattern;

/**
 * 基于配置的动态元素处理器（纯容器写入）
 */
public class DynamicElementHandler implements ElementAttachmentHandler {
	private final UnifiedElementAttachmentConfig config;

	public DynamicElementHandler(UnifiedElementAttachmentConfig config) {
		this.config = config;
	}

	@Override
	public boolean canApply(LivingEntity target, DamageSource source) {
		// 仅支持基于伤害源的统一配置
		if (config == null || !config.isDamageSourceType() ||
			config.getDamageSourceConditions() == null ||
			config.getDamageSourceConditions().getDamageSourcePatterns() == null) {
			return false;
		}

		String sourceMsgId = source.getMsgId();
		return config.getDamageSourceConditions().getDamageSourcePatterns()
				.stream()
				.anyMatch(pattern -> matchesPattern(sourceMsgId, pattern));
	}

	@Override
	public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
		int duration = (config.getEffects() != null) ? config.getEffects().getDuration() : 200;

		ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
		String elementKey = extractElementKey(config.getAttachmentType());
		container.setValue(elementKey, duration);

		// 跟踪衰减
		ElementDecaySystem.track(target);

		// 始终同步到客户端
			PacketDistributor.sendToAllPlayers(new ElementData(entityId, elementKey, duration));

		SpellElemental.LOGGER.debug("Applied element {} to entity {} with duration {} (container)", 
				elementKey, entityId, duration);
	}

	private String extractElementKey(String attachmentTypeName) {
		if (attachmentTypeName == null) return "";
		String s = attachmentTypeName.contains(":" ) ? attachmentTypeName.substring(attachmentTypeName.indexOf(":" ) + 1) : attachmentTypeName;
		return s.toLowerCase();
	}

	private boolean matchesPattern(String input, String pattern) {
		if (input == null || pattern == null) {
			return false;
		}
		if (!pattern.contains("*")) {
			return input.equals(pattern);
		}
		String regexPattern = pattern.replace("*", ".*").replace("?", ".");
		try {
			return Pattern.matches(regexPattern, input);
		} catch (Exception e) {
			SpellElemental.LOGGER.warn("Invalid pattern: {}", pattern, e);
			return false;
		}
	}

	public UnifiedElementAttachmentConfig getConfig() {
		return config;
	}
}
