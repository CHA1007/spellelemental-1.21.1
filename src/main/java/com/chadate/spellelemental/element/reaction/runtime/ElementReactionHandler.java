package com.chadate.spellelemental.element.reaction.runtime;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.element.reaction.config.ElementReactionConfig;
import com.chadate.spellelemental.element.reaction.data.ElementReactionDataLoader;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import com.chadate.spellelemental.element.attachment.attack.ElementAttachmentRegistry;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;

/**
 * 元素反应处理器
 * 负责检测、触发和执行元素反应
 */
public class ElementReactionHandler {
	
	/**
	 * 新：对当前事件伤害进行增幅，不产生额外伤害
	 */
	public static boolean tryAmplifyAnyReaction(LivingDamageEvent.Pre event) {
		LivingEntity target = event.getEntity();
		float baseDamage = (float) event.getNewDamage();
		LivingEntity attacker = null;
		if (event.getSource() != null && event.getSource().getEntity() instanceof LivingEntity le) {
			attacker = le;
		}
		// 优先按“最近附着的元素”确定方向
		String latest = ElementAttachmentRegistry.getLatestAppliedElement();
		if (latest != null && !latest.isEmpty()) {
			for (ElementReactionConfig cfg : ElementReactionDataLoader.getAllReactionsSortedByPriority()) {
				String cfgPrimary = cfg.getPrimaryElement();
				String cfgSecondary = cfg.getSecondaryElement();
				if (normalizeToBase(cfgPrimary).equals(normalizeToBase(latest))) {
					if (checkAndTriggerAmplify(attacker, target, event, cfgPrimary, cfgSecondary, baseDamage)) {
						return true;
					}
				}
			}
		}
		for (ElementReactionConfig cfg : ElementReactionDataLoader.getAllReactionsSortedByPriority()) {
			if (checkAndTriggerAmplify(attacker, target, event, cfg.getPrimaryElement(), cfg.getSecondaryElement(), baseDamage)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 旧：遍历并触发（保留，但不再额外调用）
	 */
	public static boolean tryTriggerAnyReaction(LivingEntity target, float baseDamage) {
		// 优先按“最近附着的元素”确定方向
		String latest = ElementAttachmentRegistry.getLatestAppliedElement();
		if (latest != null && !latest.isEmpty()) {
			// latest 是 element_id（如 fire、ice），与反应侧配置的 primary/secondary 可能是 *_element 或带命名空间，因此让检查函数自行归一化
			// 优先尝试：latest -> other（other 由容器中现存的另一个元素作为 secondary）
			// 简化策略：遍历已加载的反应列表，挑出 primary=latest 的，逐个检查
			for (ElementReactionConfig cfg : ElementReactionDataLoader.getAllReactionsSortedByPriority()) {
				String cfgPrimary = cfg.getPrimaryElement();
				String cfgSecondary = cfg.getSecondaryElement();
				// 若primary匹配latest，则检查触发
				if (normalizeToBase(cfgPrimary).equals(normalizeToBase(latest))) {
					if (checkAndTriggerReaction(target, cfgPrimary, cfgSecondary, baseDamage)) {
						return true;
					}
				}
			}
		}

		// 回退：按优先级遍历
		for (ElementReactionConfig cfg : ElementReactionDataLoader.getAllReactionsSortedByPriority()) {
			if (checkAndTriggerReaction(target, cfg.getPrimaryElement(), cfg.getSecondaryElement(), baseDamage)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean checkAndTriggerAmplify(LivingEntity attacker, LivingEntity target, LivingDamageEvent.Pre event, String primaryElement, String secondaryElement, float baseDamage) {
		ElementReactionConfig reactionConfig = ElementReactionDataLoader.getReactionByElements(primaryElement, secondaryElement);
		if (reactionConfig == null) return false;
		if (!canTriggerReaction(target, reactionConfig)) return false;
		executeAmplify(attacker, target, event, reactionConfig, baseDamage);
		SpellElemental.LOGGER.debug("触发元素反应(增幅): {} 在实体 {} 上", reactionConfig.getReactionName(), target.getName().getString());
		return true;
	}
	
	/**
	 * 检查并触发元素反应（默认基础伤害为 1.0f）
	 */
	public static boolean checkAndTriggerReaction(LivingEntity target, String primaryElement, String secondaryElement) {
		return checkAndTriggerReaction(target, primaryElement, secondaryElement, 1.0f);
	}

	/**
	 * 检查并触发元素反应（传入基础伤害）
	 * @param target 目标实体
	 * @param primaryElement 主导元素（可为 fire、fire_element 或 spellelemental:fire_element）
	 * @param secondaryElement 被反应元素（同上）
	 * @param baseDamage 本次事件的基础伤害（来自事件）
	 * @return 是否成功触发反应
	 */
	public static boolean checkAndTriggerReaction(LivingEntity target, String primaryElement, String secondaryElement, float baseDamage) {
		// 获取反应配置（保持与配置一致的 *_element 风格作为键）
		ElementReactionConfig reactionConfig = ElementReactionDataLoader.getReactionByElements(primaryElement, secondaryElement);
		
		if (reactionConfig == null) {
			return false;
		}
		
		// 检查触发条件
		if (!canTriggerReaction(target, reactionConfig)) {
			return false;
		}
		
		// 执行反应（旧：造成额外伤害）
		executeReaction(target, reactionConfig, baseDamage);
		
		SpellElemental.LOGGER.debug("触发元素反应: {} 在实体 {} 上", 
			reactionConfig.getReactionName(), target.getName().getString());
		
		return true;
	}

	/**
	 * 消耗元素
	 */
	private static void consumeElements(LivingEntity target, ElementReactionConfig config) {
		String primaryBase = normalizeToBase(config.getPrimaryElement());
		String secondaryBase = normalizeToBase(config.getSecondaryElement());

		int primaryCurrent = getElementAmount(target, primaryBase);
		int secondaryCurrent = getElementAmount(target, secondaryBase);
		
		int primaryNew = Math.max(primaryCurrent - config.getPrimaryConsumption(), 0);
		int secondaryNew = Math.max(secondaryCurrent - config.getSecondaryConsumption(), 0);
		
		setElementAmount(target, primaryBase, primaryNew);
		setElementAmount(target, secondaryBase, secondaryNew);
		
		if (config.getEffects() != null && config.getEffects().isElementRemoval()) {
			if (primaryNew == 0) removeElementStatus(target, primaryBase);
			if (secondaryNew == 0) removeElementStatus(target, secondaryBase);
		}
	}
	
	/**
	 * 将元素ID（可能为 *_element 或 带命名空间）归一化为基础关键字（fire、ice、water 等）
	 */
	private static String normalizeToBase(String elementId) {
		if (elementId == null) return "";
		String s = elementId.trim().toLowerCase();
		int idx = s.indexOf(':');
		if (idx >= 0) {
			s = s.substring(idx + 1);
		}
		if (s.endsWith("_element")) {
			s = s.substring(0, s.length() - "_element".length());
		}
		return s;
	}

	/**
	 * 检查是否可以触发反应
	 */
	private static boolean canTriggerReaction(LivingEntity target, ElementReactionConfig config) {
		// 使用配置中的元素ID进行归一化后查询实体的元素层数
		String primaryBase = normalizeToBase(config.getPrimaryElement());
		String secondaryBase = normalizeToBase(config.getSecondaryElement());

		int primaryAmount = getElementAmount(target, primaryBase);
		int secondaryAmount = getElementAmount(target, secondaryBase);
		
		// 基本检查：确保两个元素都存在（至少为1）
		if (primaryAmount <= 0 || secondaryAmount <= 0) {
			return false;
		}
		
		// 如果配置了具体的条件，则进行更严格的检查
		if (config.getConditions() != null) {
			if (primaryAmount < config.getConditions().getMinimumPrimaryAmount() ||
				secondaryAmount < config.getConditions().getMinimumSecondaryAmount()) {
				return false;
			}
		}
		
		// 环境条件（占位，总是通过）
		if (!checkEnvironmentalConditions(target, config)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 计算反应最终伤害（不直接造成伤害）
	 */
	private static float calculateFinalDamage(LivingEntity attacker, LivingEntity target, ElementReactionConfig config, float baseDamage) {
		float finalDamage;
		String type = config.getEffects() != null ? config.getEffects().getDamageType() : "";
		float mult = config.getDamageMultiplier() <= 0 ? 1.0f : config.getDamageMultiplier();
		float astral = 0f;
		try {
			if (attacker != null) {
				astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
			}
		} catch (Exception ignored) {}
		switch (type) {
			case "amplified_reaction":
				finalDamage = baseDamage * mult * ReactionInjuryFormula.AmplifiedReactionBonus(astral);
				break;
			case "overload_reaction":
				finalDamage = ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
				break;
			default:
				finalDamage = baseDamage * mult;
		}
		return finalDamage;
	}
	
	/**
	 * 根据字符串选择范围伤害的DamageSource
	 */
	private static DamageSource resolveAreaDamageSource(LivingEntity attacker, LivingEntity victim, String key) {
		if (key == null || key.isEmpty()) {
			return victim.damageSources().magic();
		}
		switch (key.toLowerCase()) {
			case "lightning":
				return victim.damageSources().lightningBolt();
			case "fire":
				return victim.damageSources().inFire();
			case "lava":
				return victim.damageSources().lava();
			case "magic":
			default:
				return victim.damageSources().magic();
		}
	}
	
	/**
	 * 执行元素反应（增幅当前事件伤害）
	 */
	private static void executeAmplify(LivingEntity attacker, LivingEntity target, LivingDamageEvent.Pre event, ElementReactionConfig config, float baseDamage) {
		// 消耗元素
		consumeElements(target, config);
		
		String type = config.getEffects() != null ? config.getEffects().getDamageType() : "";
		float computed = calculateFinalDamage(attacker, target, config, baseDamage);
		String areaDamageTypeKey = config.getEffects() != null ? config.getEffects().getDamageSource() : null;
		
		if ("overload_reaction".equals(type)) {
			// 聚变（超载）：按照公式对范围内单位造成一次伤害；不修改当前事件伤害
			float radius = 3.0f;
			boolean includeSelf = false;
			if (config.getEffects() != null) {
				if (config.getEffects().getAreaRadius() > 0) {
					radius = config.getEffects().getAreaRadius();
				}
				includeSelf = config.getEffects().isIncludeSelf();
			}
			applyAreaDamageWithSelfOption(attacker, target, computed, radius, includeSelf, areaDamageTypeKey);
		} else if (config.getEffects() != null && config.getEffects().isAreaDamage()) {
			// 旧配置：显式area_damage为true时，范围伤害
			float radius = config.getEffects().getAreaRadius();
			if (radius <= 0) radius = 3.0f;
			applyAreaDamageWithSelfOption(attacker, target, computed, radius, false, areaDamageTypeKey);
		} else {
			// 默认：增幅当前事件伤害
			event.setNewDamage(computed);
		}
		
		// 播放视觉/音效/粒子
		playVisualEffects(target, config);
		playSoundEffects(target, config);
		spawnParticleEffects(target, config);
	}
	
	/**
	 * 执行元素反应（旧：产生独立的额外伤害）
	 */
	private static void executeReaction(LivingEntity target, ElementReactionConfig config, float baseDamage) {
		// 消耗元素
		consumeElements(target, config);
		
		// 应用反应效果 → 旧逻辑：额外伤害
		applyReactionEffects(target, config, baseDamage);
		
		// 播放视觉效果
		playVisualEffects(target, config);
		
		// 播放音效
		playSoundEffects(target, config);
		
		// 生成粒子效果
		spawnParticleEffects(target, config);
	}
	
	/**
	 * 应用反应效果（旧：造成额外伤害）
	 */
	private static void applyReactionEffects(LivingEntity target, ElementReactionConfig config, float baseDamage) {
		float finalDamage;
		String type = config.getEffects() != null ? config.getEffects().getDamageType() : "";
		float mult = config.getDamageMultiplier() <= 0 ? 1.0f : config.getDamageMultiplier();
		float astral = 0f;
		try {
			astral = (float) target.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
		} catch (Exception ignored) {}
		// 这里示例按 reaction_type 选择公式，可继续扩展映射
		switch (type) {
			case "amplified_reaction":
				finalDamage = baseDamage * mult * ReactionInjuryFormula.AmplifiedReactionBonus(astral);
				break;
			case "overload_reaction":
				finalDamage = ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
				break;
			default:
				finalDamage = baseDamage * mult;
		}
		
		if (config.getEffects() != null && config.getEffects().isAreaDamage()) {
			applyAreaDamage(target, finalDamage, config.getEffects().getAreaRadius());
		} else {
			target.hurt(target.damageSources().magic(), finalDamage);
		}
		
		if (config.getEffects() != null && config.getEffects().getStatusEffects() != null) {
			for (ElementReactionConfig.StatusEffect statusEffect : config.getEffects().getStatusEffects()) {
				applyStatusEffect(target, statusEffect);
			}
		}
	}

	private static void applyAreaDamage(LivingEntity target, float damage, float radius) {
		Level level = target.level();
		Vec3 center = target.position();
		AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius,
						center.x + radius, center.y + radius, center.z + radius);
		
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
		
		for (LivingEntity entity : entities) {
			if (entity.isAlive() && !entity.isSpectator() && entity != target) {
				entity.hurt(entity.damageSources().magic(), damage);
			}
		}
	}

	private static void applyAreaDamageWithSelfOption(LivingEntity attacker, LivingEntity target, float damage, float radius, boolean includeSelf, String damageTypeKey) {
		Level level = target.level();
		Vec3 center = target.position();
		AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius,
						center.x + radius, center.y + radius, center.z + radius);
		
		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
		
		for (LivingEntity entity : entities) {
			DamageSource ds = resolveAreaDamageSource(attacker, entity, damageTypeKey);
			if (includeSelf && entity == target) {
				entity.hurt(ds, damage);
			} else if (entity.isAlive() && !entity.isSpectator() && entity != target) {
				entity.hurt(ds, damage);
			}
		}
	}
	
	private static void applyStatusEffect(LivingEntity target, ElementReactionConfig.StatusEffect statusEffect) {
		SpellElemental.LOGGER.debug("应用状态效果: {} 到实体 {}", 
			statusEffect.getEffectId(), target.getName().getString());
	}
	
	private static void playVisualEffects(LivingEntity target, ElementReactionConfig config) {
		if (config.getVisualEffects() != null) {
			SpellElemental.LOGGER.debug("播放视觉效果: {} 到实体 {}", 
				config.getVisualEffects().getScreenEffect(), target.getName().getString());
		}
	}
	
	private static void playSoundEffects(LivingEntity target, ElementReactionConfig config) {
		if (config.getSoundEffects() != null) {
			SpellElemental.LOGGER.debug("播放音效: {} 到实体 {}", 
				config.getSoundEffects().getReactionSound(), target.getName().getString());
		}
	}
	
	private static void spawnParticleEffects(LivingEntity target, ElementReactionConfig config) {
		if (config.getParticleEffects() != null) {
			SpellElemental.LOGGER.debug("生成粒子效果: {} 在实体 {} 位置", 
				config.getParticleEffects().getParticleType(), target.getName().getString());
		}
	}
	
	private static boolean checkEnvironmentalConditions(LivingEntity target, ElementReactionConfig config) {
		if (config.getConditions() == null) return true;
		if (config.getConditions().getEnvironmentalRequirements() == null ||
			config.getConditions().getEnvironmentalRequirements().isEmpty()) {
			return true;
		}
		return true;
	}
	
	private static int getElementAmount(LivingEntity entity, String baseElement) {
		try {
			return ReactionEvent.getElementAttachment(entity, baseElement);
		} catch (Exception e) {
			return 0;
		}
	}
	
	private static void setElementAmount(LivingEntity entity, String baseElement, int amount) {
		try {
			ReactionEvent.setElementAttachment(entity, baseElement, amount);
		} catch (Exception e) {
			SpellElemental.LOGGER.error("设置元素数量失败: {} -> {}", baseElement, amount, e);
		}
	}
	
	private static void removeElementStatus(LivingEntity entity, String baseElement) {
		setElementAmount(entity, baseElement, 0);
	}
} 