package com.chadate.spellelemental.element.reaction.runtime;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.element.reaction.config.ElementReactionConfig;
import com.chadate.spellelemental.element.reaction.data.ElementReactionDataLoader;
import com.chadate.spellelemental.event.element.ReactionEvent;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import com.chadate.spellelemental.element.attachment.attack.ElementAttachmentRegistry;
import com.chadate.spellelemental.util.DamageAttachmentGuards;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.List;

/**
 * 元素反应处理器
 * 负责检测、触发和执行元素反应
 */
public class ElementReactionHandler {
	
	/**
     * 对当前事件伤害进行增幅，不产生额外伤害
     */
	public static void tryAmplifyAnyReaction(LivingDamageEvent.Pre event) {
		LivingEntity target = event.getEntity();
		float baseDamage = (float) event.getNewDamage();
		LivingEntity attacker = null;
        event.getSource();
        if (event.getSource().getEntity() instanceof LivingEntity le) {
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
						return;
					}
				}
			}
		}
		for (ElementReactionConfig cfg : ElementReactionDataLoader.getAllReactionsSortedByPriority()) {
			if (checkAndTriggerAmplify(attacker, target, event, cfg.getPrimaryElement(), cfg.getSecondaryElement(), baseDamage)) {
				return;
			}
		}
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
	 * 将元素ID（可能带命名空间）归一化为存储键名（与附着存储保持一致）
	 */
	private static String normalizeToBase(String elementId) {
		if (elementId == null) return "";
		String s = elementId.trim().toLowerCase();
		int idx = s.indexOf(':');
		if (idx >= 0) {
			s = s.substring(idx + 1);
		}
		// 不再移除 "_element" 后缀，保持与附着存储的键名一致
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
	 * 根据字符串选择范围伤害的DamageSource
	 */
	private static DamageSource resolveAreaDamageSource(LivingEntity attacker, LivingEntity victim, String key) {
		if (key == null || key.isEmpty()) {
			return victim.damageSources().magic();
		}
		// 先尝试解析命名空间 DamageType，例如 "irons_spellbooks:fire_magic" 或 "minecraft:in_fire"
		if (key.contains(":")) {
			try {
				ResourceLocation id = ResourceLocation.tryParse(key);
				if (id != null) {
					var reg = victim.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
					ResourceKey<DamageType> rkey = ResourceKey.create(Registries.DAMAGE_TYPE, id);
					var holderOpt = reg.getHolder(rkey);
					if (holderOpt.isPresent()) {
						// 使用 ResourceKey 版本的 API，兼容当前 MC/NeoForge 签名
						return victim.damageSources().source(rkey);
					}
				}
			} catch (Exception ignored) {}
			// 命名空间解析失败则回退为 magic
		}
		return victim.damageSources().magic();
	}
	
	/**
	 * 执行元素反应（增幅当前事件伤害）
	 */
	private static void executeAmplify(LivingEntity attacker, LivingEntity target, LivingDamageEvent.Pre event, ElementReactionConfig config, float baseDamage) {
		SpellElemental.LOGGER.info("=== 元素反应触发 ===");
		SpellElemental.LOGGER.info("反应名称: {}", config.getReactionName());
		SpellElemental.LOGGER.info("反应ID: {}", config.getReactionId());
		SpellElemental.LOGGER.info("反应优先级: {}", config.getPriority());
		SpellElemental.LOGGER.info("攻击者: {}", attacker != null ? attacker.getName().getString() : "无");
		SpellElemental.LOGGER.info("目标实体: {}", target.getName().getString());
		SpellElemental.LOGGER.info("基础伤害: {}", baseDamage);
		
		// 消耗元素
		consumeElements(target, config);
		
		// 执行效果处理
		if (config.getEffects() != null && config.getEffects().hasEffects()) {
			SpellElemental.LOGGER.info("发现效果配置，效果数量: {}", config.getEffects().getReactionEffects().size());
			// 将变体的元素对传入，供 DOT 做逐tick元素校验
			executeEffects(attacker, target, event, config.getEffects().getReactionEffects(), baseDamage,
					config.getPrimaryElement(), config.getSecondaryElement());
		} else {
			SpellElemental.LOGGER.warn("未发现效果配置或效果为空，反应可能无法正常工作");
		}
		
        // 触发后概率性附着元素
        applyGrantElements(attacker, target, config);

        // 播放视觉/音效/粒子
        playVisualEffects(target, config);
        playSoundEffects(target, config);
        spawnParticleEffects(target, config);
    }

    /**
     * 处理 grant_elements：为指定实体按概率附着元素层数
     */
    private static void applyGrantElements(LivingEntity attacker, LivingEntity target, ElementReactionConfig config) {
        if (config.getEffects() == null || config.getEffects().getGrantElements() == null
                || config.getEffects().getGrantElements().isEmpty()) {
            return;
        }
        for (ElementReactionConfig.GrantElement g : config.getEffects().getGrantElements()) {
            if (g == null || g.getElement() == null || g.getElement().isEmpty()) continue;
            float chance = Math.max(0f, Math.min(1f, g.getChance()));
            if (target.getRandom().nextFloat() > chance) continue;

            String elemKey = normalizeToBase(g.getElement());
            LivingEntity receiver = ("attacker".equalsIgnoreCase(g.getTarget()) ? attacker : target);
            if (receiver == null) continue;

            int amount = Math.max(0, g.getAmount());
            String mode = g.getMode() == null ? "add" : g.getMode().toLowerCase();
            if ("refresh".equals(mode)) {
                setElementAmount(receiver, elemKey, amount);
                SpellElemental.LOGGER.info("grant_elements: 刷新 {} 至 {} 到 {}", elemKey, amount,
                        receiver == target ? "target" : "attacker");
            } else {
                int cur = getElementAmount(receiver, elemKey);
                setElementAmount(receiver, elemKey, cur + amount);
                SpellElemental.LOGGER.info("grant_elements: 增加 {} +{} 到 {} (原 {} -> 新 {})", elemKey, amount,
                        receiver == target ? "target" : "attacker", cur, cur + amount);
            }
        }
    }
	
	/**
	 * 执行效果列表
	 */
	private static void executeEffects(LivingEntity attacker, LivingEntity target, LivingDamageEvent.Pre event,
                                       List<ElementReactionConfig.ReactionEffect> effects, float baseDamage) {
        // 兼容旧调用：不携带元素对信息
        executeEffects(attacker, target, event, effects, baseDamage, null, null);
    }

    /**
     * 执行效果列表（可携带当前变体的元素对，便于 DOT 逐tick校验）
     */
    private static void executeEffects(LivingEntity attacker, LivingEntity target, LivingDamageEvent.Pre event,
                                       List<ElementReactionConfig.ReactionEffect> effects, float baseDamage,
                                       String primaryElement, String secondaryElement) {
        if (effects.isEmpty()) {
            return;
        }
		
		// 按优先级排序效果
		effects.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
		
		float currentDamage = baseDamage;
		boolean damageModified = false;
		float originalDamage = baseDamage;
		
		SpellElemental.LOGGER.info("=== 元素反应伤害计算开始 ===");
		SpellElemental.LOGGER.info("目标实体: {}", target.getName().getString());
		SpellElemental.LOGGER.info("原始伤害: {}", originalDamage);
		SpellElemental.LOGGER.info("效果数量: {}", effects.size());
		
		for (ElementReactionConfig.ReactionEffect effect : effects) {
			// 检查效果条件
			if (!checkEffectConditions(attacker, target, effect, currentDamage)) {
				SpellElemental.LOGGER.debug("效果条件不满足，跳过效果: {}", effect.getEffectType());
				continue;
			}
			
			String effectType = effect.getEffectType();
			float effectDamage = calculateDamage(attacker, target, effect, currentDamage);
			float effectMultiplier = effect.getDamageMultiplier();
			
			SpellElemental.LOGGER.debug("执行效果: {}, 当前伤害: {}, 计算后伤害: {}", effectType, currentDamage, effectDamage);
			
			if ("amplified_reaction".equals(effectType)) {
				// 增幅效果：修改当前事件伤害
				float previousDamage = currentDamage;
				currentDamage = effectDamage;
				damageModified = true;
				
				// 获取星耀祝福数值
				float astral = 0f;
				try {
					if (attacker != null) {
						astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
					}
				} catch (Exception ignored) {}
				
				SpellElemental.LOGGER.info("=== 增幅反应触发 ===");
				SpellElemental.LOGGER.info("反应类型: {}", effectType);
				SpellElemental.LOGGER.info("反应倍率: {}", effectMultiplier);
				SpellElemental.LOGGER.info("星耀祝福: {}", astral);
				SpellElemental.LOGGER.info("增幅前伤害: {}", previousDamage);
				SpellElemental.LOGGER.info("增幅后伤害: {}", currentDamage);
				SpellElemental.LOGGER.info("伤害增幅: {} -> {} (增加 {})", previousDamage, currentDamage, currentDamage - previousDamage);
			} else if ("area_damage".equals(effectType)) {
				// 范围效果：造成范围伤害，不修改当前事件伤害
				float radius = effect.getAreaRadius() > 0 ? effect.getAreaRadius() : 3.0f;
				boolean includeSelf = effect.isIncludeSelf();
				String damageSource = effect.getDamageSource();
				
				// 获取星耀祝福数值
				float astral = 0f;
				try {
					if (attacker != null) {
						astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
					}
				} catch (Exception ignored) {}
				
				SpellElemental.LOGGER.info("=== 范围伤害反应触发 ===");
				SpellElemental.LOGGER.info("反应类型: {}", effectType);
				SpellElemental.LOGGER.info("反应倍率: {}", effectMultiplier);
				SpellElemental.LOGGER.info("星耀祝福: {}", astral);
				SpellElemental.LOGGER.info("范围伤害: {} (半径: {})", effectDamage, radius);
				
				applyAreaDamageWithSelfOption(attacker, target, effectDamage, radius, includeSelf, damageSource);
			} else if ("dot".equals(effectType)) {
                // 注册数据化 DOT 实例（不修改当前事件伤害）
                if (primaryElement != null && secondaryElement != null) {
                    // 使用当前变体元素对作为必需元素，启用逐tick校验
                    String reqA = normalizeToBase(primaryElement);
                    String reqB = normalizeToBase(secondaryElement);
                    com.chadate.spellelemental.event.element.DotSystem.registerDot(attacker, target, effect, reqA, reqB);
                } else {
                    com.chadate.spellelemental.event.element.DotSystem.registerDot(attacker, target, effect);
                }
                SpellElemental.LOGGER.info("=== 持续伤害(DOT)已注册 === interval={} ticks, tickDamage={}, duration={} ticks",
                        effect.getIntervalTicks(), effect.getTickDamage(), effect.getDurationTicks());
            } else {
				// 默认：直接伤害目标
				DamageSource ds = resolveAreaDamageSource(attacker, target, effect.getDamageSource());
				
				SpellElemental.LOGGER.info("=== 直接伤害反应触发 ===");
				SpellElemental.LOGGER.info("反应类型: {}", effectType);
				SpellElemental.LOGGER.info("反应倍率: {}", effectMultiplier);
				SpellElemental.LOGGER.info("直接伤害: {}", effectDamage);
				
				DamageAttachmentGuards.runAsNonAttachable(() -> target.hurt(ds, effectDamage));
			}
		}
		
		// 如果有增幅效果，更新事件伤害
		if (damageModified) {
			SpellElemental.LOGGER.info("=== 最终伤害更新 ===");
			SpellElemental.LOGGER.info("原始伤害: {}", originalDamage);
			SpellElemental.LOGGER.info("最终伤害: {}", currentDamage);
			SpellElemental.LOGGER.info("总伤害增幅: {} -> {} (增加 {})", originalDamage, currentDamage, currentDamage - originalDamage);
			SpellElemental.LOGGER.info("=== 元素反应伤害计算结束 ===");
			
			event.setNewDamage(currentDamage);
		} else {
			SpellElemental.LOGGER.info("=== 元素反应伤害计算结束 ===");
			SpellElemental.LOGGER.info("无伤害增幅，保持原始伤害: {}", originalDamage);
		}
	}
	
	/**
	 * 计算伤害
	 */
	private static float calculateDamage(LivingEntity attacker, LivingEntity target, ElementReactionConfig.ReactionEffect effect, float baseDamage) {
        String effectType = effect.getEffectType();
        float mult = effect.getDamageMultiplier() <= 0 ? 1.0f : effect.getDamageMultiplier();
        float astral = 0f;

        try {
            if (attacker != null) {
                astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
            }
        } catch (Exception ignored) {}

        // 解析伤害模式（优先使用 damage_mode；未指定则按旧的 effect_type 推断）
        String mode = effect.getDamageMode();
        if (mode == null || mode.isEmpty()) {
            if ("amplified_reaction".equals(effectType)) mode = "amplified";
            else if ("overload_reaction".equals(effectType) || "area_damage".equals(effectType)) mode = "overload";
            else mode = "default";
        } else {
            mode = mode.toLowerCase();
        }

        float finalDamage;
        switch (mode) {
            case "fixed": {
                finalDamage = Math.max(0f, effect.getFixedDamage());
                SpellElemental.LOGGER.debug("固定伤害计算: {} (fixed)", finalDamage);
                break;
            }
            case "amplified": {
                float bonus = ReactionInjuryFormula.AmplifiedReactionBonus(astral);
                finalDamage = baseDamage * mult * bonus;
                SpellElemental.LOGGER.debug("增幅伤害计算: base={} mult={} astral={} bonus={} -> {}", baseDamage, mult, astral, bonus, finalDamage);
                break;
            }
            case "overload": {
                finalDamage = ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
                SpellElemental.LOGGER.debug("超载伤害计算: base={} mult={} astral={} -> {}", baseDamage, mult, astral, finalDamage);
                break;
            }
            case "fusion": {
                // 聚变按照用户要求，复用超载公式
                finalDamage = ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
                SpellElemental.LOGGER.debug("聚变(使用超载公式)伤害计算: base={} mult={} astral={} -> {}", baseDamage, mult, astral, finalDamage);
                break;
            }
            default: {
                finalDamage = baseDamage * mult;
                SpellElemental.LOGGER.debug("默认伤害计算: base={} mult={} -> {}", baseDamage, mult, finalDamage);
                break;
            }
        }
        return finalDamage;
    }
	

	
	/**
	 * 检查效果触发条件
	 */
	private static boolean checkEffectConditions(LivingEntity attacker, LivingEntity target, ElementReactionConfig.ReactionEffect effect, float currentDamage) {
		ElementReactionConfig.EffectConditions conditions = effect.getConditions();
		if (conditions == null) {
			return true; // 无条件限制
		}
		
		// 检查伤害范围
		if (currentDamage < conditions.getMinimumDamage() || currentDamage > conditions.getMaximumDamage()) {
			return false;
		}
		
		// 检查必需元素
		if (conditions.getRequiredElements() != null && !conditions.getRequiredElements().isEmpty()) {
			for (String requiredElement : conditions.getRequiredElements()) {
				String baseElement = normalizeToBase(requiredElement);
				if (getElementAmount(target, baseElement) <= 0) {
					return false;
				}
			}
		}
		
		// 检查排除元素
		if (conditions.getExcludedElements() != null && !conditions.getExcludedElements().isEmpty()) {
			for (String excludedElement : conditions.getExcludedElements()) {
				String baseElement = normalizeToBase(excludedElement);
				if (getElementAmount(target, baseElement) > 0) {
					return false;
			}
		}
	}
		
		return true;
	}
	

	private static void applyAreaDamage(LivingEntity target, float damage, float radius) {
     Level level = target.level();
     Vec3 center = target.position();
     AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius,
                     center.x + radius, center.y + radius, center.z + radius);
     
     List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
     
     for (LivingEntity entity : entities) {
         if (entity.isAlive() && !entity.isSpectator() && entity != target) {
             DamageAttachmentGuards.runAsNonAttachable(() -> entity.hurt(entity.damageSources().magic(), damage));
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
         // 跳过非存活实体和旁观者
         if (!entity.isAlive() || entity.isSpectator()) {
             continue;
         }
         // 处理目标实体（被攻击者）
         if (entity == target) {
             if (includeSelf) {
                 DamageSource ds = resolveAreaDamageSource(attacker, entity, damageTypeKey);
                 DamageAttachmentGuards.runAsNonAttachable(() -> entity.hurt(ds, damage));
             }
             continue;
         }
         // 跳过攻击者，避免自伤
         if (entity == attacker) {
             continue;
         }
         // 对其他范围内的实体造成伤害
         DamageSource ds = resolveAreaDamageSource(attacker, entity, damageTypeKey);
         DamageAttachmentGuards.runAsNonAttachable(() -> entity.hurt(ds, damage));
     }
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