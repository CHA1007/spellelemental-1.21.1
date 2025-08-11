package com.chadate.spellelemental.element.attachment.attack;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.config.ElementAttachmentConfig;
import com.chadate.spellelemental.client.network.custom.ElementData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * 基于配置的动态元素处理器
 * 根据 JSON 配置动态创建元素附着处理逻辑
 */
public class DynamicElementHandler implements ElementAttachmentHandler {
    private final ElementAttachmentConfig config;
    private final AttachmentType<ElementsAttachment> attachmentType;

    public DynamicElementHandler(ElementAttachmentConfig config) {
        this.config = config;
        this.attachmentType = getAttachmentTypeByName(config.getAttachmentType());
    }

    @Override
    public boolean canApply(LivingEntity target, DamageSource source) {
        if (config.getTriggerConditions() == null || 
            config.getTriggerConditions().getDamageSourcePatterns() == null) {
            return false;
        }

        String sourceMsgId = source.getMsgId();
        return config.getTriggerConditions().getDamageSourcePatterns()
                .stream()
                .anyMatch(pattern -> matchesPattern(sourceMsgId, pattern));
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        if (attachmentType == null) {
            SpellElemental.LOGGER.warn("Attachment type not found for element: {}", config.getElementId());
            return;
        }

        ElementsAttachment attachment = target.getData(attachmentType);
        int duration = config.getEffects() != null ? config.getEffects().getDuration() : 200;
        attachment.setValue(duration);

        // 网络同步
        if (config.getEffects() != null && config.getEffects().isNetworkSync()) {
            PacketDistributor.sendToAllPlayers(
                new ElementData(entityId, config.getElementId() + "_element", attachment.getValue())
            );
        }

        SpellElemental.LOGGER.debug("Applied element {} to entity {} with duration {}", 
            config.getElementId(), entityId, duration);
    }

    /**
     * 根据附着类型名称获取对应的 AttachmentType
     */
    private AttachmentType<ElementsAttachment> getAttachmentTypeByName(String typeName) {
        if (typeName == null) return null;

        // 移除命名空间前缀
        String elementName = typeName.contains(":") ? 
            typeName.substring(typeName.indexOf(":") + 1) : typeName;

        // 映射到现有的 AttachmentType
        switch (elementName) {
            case "fire_element":
                return SpellAttachments.FIRE_ELEMENT.get();
            case "ice_element":
                return SpellAttachments.ICE_ELEMENT.get();
            case "lightning_element":
                return SpellAttachments.LIGHTNING_ELEMENT.get();
            case "water_element":
                return SpellAttachments.WATER_ELEMENT.get();
            case "nature_element":
                return SpellAttachments.NATURE_ELEMENT.get();
            case "holy_element":
                return SpellAttachments.HOLY_ELEMENT.get();
            case "blood_element":
                return SpellAttachments.BLOOD_ELEMENT.get();
            case "ender_element":
                return SpellAttachments.ENDER_ELEMENT.get();
            default:
                SpellElemental.LOGGER.warn("Unknown attachment type: {}", typeName);
                return null;
        }
    }

    /**
     * 模式匹配工具方法
     * 支持通配符 * 和精确匹配
     */
    private boolean matchesPattern(String input, String pattern) {
        if (input == null || pattern == null) {
            return false;
        }

        // 精确匹配
        if (!pattern.contains("*")) {
            return input.equals(pattern);
        }

        // 通配符匹配
        String regexPattern = pattern
            .replace("*", ".*")
            .replace("?", ".");
        
        try {
            return Pattern.matches(regexPattern, input);
        } catch (Exception e) {
            SpellElemental.LOGGER.warn("Invalid pattern: {}", pattern, e);
            return false;
        }
    }

    public ElementAttachmentConfig getConfig() {
        return config;
    }
}
