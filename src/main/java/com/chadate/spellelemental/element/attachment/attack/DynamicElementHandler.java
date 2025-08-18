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
public record DynamicElementHandler(UnifiedElementAttachmentConfig config) implements ElementAttachmentHandler {

    @Override
    public boolean canApply(LivingEntity target, DamageSource source, float damageAmount) {
        // 仅支持基于伤害源的统一配置
        if (config == null || !config.isDamageSourceType() ||
                config.getDamageSourceConditions() == null ||
                config.getDamageSourceConditions().getDamageSourcePatterns() == null) {
            return false;
        }

        String sourceMsgId = source.getMsgId();
        boolean patternMatched = config.getDamageSourceConditions().getDamageSourcePatterns()
                .stream()
                .anyMatch(pattern -> matchesPattern(sourceMsgId, pattern));
        if (!patternMatched) return false;

        // 最低伤害与概率
        UnifiedElementAttachmentConfig.EffectConfig effects = config.getEffects();
        if (effects != null) {
            float min = effects.getMinDamage();
            if (min > 0f && damageAmount < min) {
                return false;
            }
            float chance = effects.getApplyChance();
            if (chance < 1.0f) {
                float roll = target.getRandom().nextFloat();
                if (roll >= Math.max(0f, Math.min(1f, chance))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        int duration = (config.getEffects() != null) ? config.getEffects().getDuration() : 200;

        ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
        // 统一容器键：仅使用 element_id（小写）。若缺失则不进行写入。
        String elementKey = config.getElementId();
        if (elementKey == null || elementKey.isBlank()) {
            SpellElemental.LOGGER.debug("Skip applying element: element_id is missing for entity {}", entityId);
            return;
        }
        elementKey = elementKey.toLowerCase();
        container.setValue(elementKey, duration);
        // 记录最近附着时间（同体反应方向判定）
        long gameTime = target.level().getGameTime();
        container.markApplied(elementKey, gameTime);

        // 跟踪衰减
        ElementDecaySystem.track(target);

        // 始终同步到客户端
        PacketDistributor.sendToAllPlayers(new ElementData(entityId, elementKey, duration));

        SpellElemental.LOGGER.debug("Applied element {} to entity {} with duration {} (container)",
                elementKey, entityId, duration);
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


}
