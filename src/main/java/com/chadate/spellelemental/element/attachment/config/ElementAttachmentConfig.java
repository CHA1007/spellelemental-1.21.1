package com.chadate.spellelemental.element.attachment.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 元素附着配置数据类
 * 用于从 JSON 数据包中加载元素附着规则
 */
public class ElementAttachmentConfig {
    @SerializedName("element_id")
    private String elementId;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("attachment_type")
    private String attachmentType;
    @SerializedName("trigger_conditions")
    private TriggerConditions triggerConditions;
    private EffectProperties effects;
    private VisualProperties visual;

    // 构造函数
    public ElementAttachmentConfig() {}

    public ElementAttachmentConfig(String elementId, String displayName, String attachmentType) {
        this.elementId = elementId;
        this.displayName = displayName;
        this.attachmentType = attachmentType;
    }

    // Getters and Setters
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public TriggerConditions getTriggerConditions() {
        return triggerConditions;
    }

    public void setTriggerConditions(TriggerConditions triggerConditions) {
        this.triggerConditions = triggerConditions;
    }

    public EffectProperties getEffects() {
        return effects;
    }

    public void setEffects(EffectProperties effects) {
        this.effects = effects;
    }

    public VisualProperties getVisual() {
        return visual;
    }

    public void setVisual(VisualProperties visual) {
        this.visual = visual;
    }

    /**
     * 触发条件配置
     */
    public static class TriggerConditions {
        @SerializedName("damage_source_patterns")
        private List<String> damageSourcePatterns;
        @SerializedName("spell_schools")
        private List<String> spellSchools;
        @SerializedName("required_tags")
        private List<String> requiredTags;

        public TriggerConditions() {}

        public List<String> getDamageSourcePatterns() {
            return damageSourcePatterns;
        }

        public void setDamageSourcePatterns(List<String> damageSourcePatterns) {
            this.damageSourcePatterns = damageSourcePatterns;
        }

        public List<String> getSpellSchools() {
            return spellSchools;
        }

        public void setSpellSchools(List<String> spellSchools) {
            this.spellSchools = spellSchools;
        }

        public List<String> getRequiredTags() {
            return requiredTags;
        }

        public void setRequiredTags(List<String> requiredTags) {
            this.requiredTags = requiredTags;
        }
    }

    /**
     * 效果属性配置
     */
    public static class EffectProperties {
        private int duration = 200;
        @SerializedName("stack_limit")
        private int stackLimit = 10;
        @SerializedName("decay_rate")
        private int decayRate = 1;
        @SerializedName("network_sync")
        private boolean networkSync = true;

        public EffectProperties() {}

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getStackLimit() {
            return stackLimit;
        }

        public void setStackLimit(int stackLimit) {
            this.stackLimit = stackLimit;
        }

        public int getDecayRate() {
            return decayRate;
        }

        public void setDecayRate(int decayRate) {
            this.decayRate = decayRate;
        }

        public boolean isNetworkSync() {
            return networkSync;
        }

        public void setNetworkSync(boolean networkSync) {
            this.networkSync = networkSync;
        }
    }

    /**
     * 视觉效果配置
     */
    public static class VisualProperties {
        private String icon;
        private String color;
        @SerializedName("particle_effect")
        private String particleEffect;

        public VisualProperties() {}

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getParticleEffect() {
            return particleEffect;
        }

        public void setParticleEffect(String particleEffect) {
            this.particleEffect = particleEffect;
        }
    }
}
