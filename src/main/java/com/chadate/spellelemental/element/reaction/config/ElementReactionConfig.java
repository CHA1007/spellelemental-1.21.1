package com.chadate.spellelemental.element.reaction.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 元素反应配置数据类
 * 用于从 JSON 数据包中加载元素反应规则
 * 支持冰火融化反应等复杂的元素交互
 */
public class ElementReactionConfig {
    
    @SerializedName("reaction_id")
    private String reactionId;
    
    @SerializedName("reaction_name")
    private String reactionName;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("primary_element")
    private String primaryElement;
    
    @SerializedName("secondary_element")
    private String secondaryElement;
    
    @SerializedName("primary_consumption")
    private int primaryConsumption;
    
    @SerializedName("secondary_consumption")
    private int secondaryConsumption;
    
    @SerializedName("effects")
    private ReactionEffects effects;
    
    @SerializedName("damage_multiplier")
    private float damageMultiplier;
    
    @SerializedName("reaction_conditions")
    private ReactionConditions conditions;
    
    @SerializedName("visual_effects")
    private VisualEffects visualEffects;
    
    @SerializedName("sound_effects")
    private SoundEffects soundEffects;
    
    @SerializedName("particle_effects")
    private ParticleEffects particleEffects;

    @SerializedName("priority")
    private int priority = 0;
    
    // 构造函数
    public ElementReactionConfig() {}
    
    public ElementReactionConfig(String reactionId, String reactionName, String description,
                                String primaryElement, String secondaryElement,
                                int primaryConsumption, int secondaryConsumption) {
        this.reactionId = reactionId;
        this.reactionName = reactionName;
        this.description = description;
        this.primaryElement = primaryElement;
        this.secondaryElement = secondaryElement;
        this.primaryConsumption = primaryConsumption;
        this.secondaryConsumption = secondaryConsumption;
    }
    
    // Getters and Setters
    public String getReactionId() {
        return reactionId;
    }
    
    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }
    
    public String getReactionName() {
        return reactionName;
    }
    
    public void setReactionName(String reactionName) {
        this.reactionName = reactionName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPrimaryElement() {
        return primaryElement;
    }
    
    public void setPrimaryElement(String primaryElement) {
        this.primaryElement = primaryElement;
    }
    
    public String getSecondaryElement() {
        return secondaryElement;
    }
    
    public void setSecondaryElement(String secondaryElement) {
        this.secondaryElement = secondaryElement;
    }
    
    public int getPrimaryConsumption() {
        return primaryConsumption;
    }
    
    public void setPrimaryConsumption(int primaryConsumption) {
        this.primaryConsumption = primaryConsumption;
    }
    
    public int getSecondaryConsumption() {
        return secondaryConsumption;
    }
    
    public void setSecondaryConsumption(int secondaryConsumption) {
        this.secondaryConsumption = secondaryConsumption;
    }
    
    public ReactionEffects getEffects() {
        return effects;
    }
    
    public void setEffects(ReactionEffects effects) {
        this.effects = effects;
    }
    
    public float getDamageMultiplier() {
        return damageMultiplier;
    }
    
    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }
    
    public ReactionConditions getConditions() {
        return conditions;
    }
    
    public void setConditions(ReactionConditions conditions) {
        this.conditions = conditions;
    }
    
    public VisualEffects getVisualEffects() {
        return visualEffects;
    }
    
    public void setVisualEffects(VisualEffects visualEffects) {
        this.visualEffects = visualEffects;
    }
    
    public SoundEffects getSoundEffects() {
        return soundEffects;
    }
    
    public void setSoundEffects(SoundEffects soundEffects) {
        this.soundEffects = soundEffects;
    }
    
    public ParticleEffects getParticleEffects() {
        return particleEffects;
    }
    
    public void setParticleEffects(ParticleEffects particleEffects) {
        this.particleEffects = particleEffects;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * 反应效果配置
     */
    public static class ReactionEffects {
        @SerializedName("reaction_effects")
        private List<ReactionEffect> reactionEffects;
        
        @SerializedName("status_effects")
        private List<StatusEffect> statusEffects;
        
        @SerializedName("element_removal")
        private boolean elementRemoval;
        
        @SerializedName("cooldown")
        private int cooldown;
        
        // Getters and Setters
        public List<ReactionEffect> getReactionEffects() {
            return reactionEffects;
        }
        
        public void setReactionEffects(List<ReactionEffect> reactionEffects) {
            this.reactionEffects = reactionEffects;
        }
        
        public List<StatusEffect> getStatusEffects() {
            return statusEffects;
        }
        
        public void setStatusEffects(List<StatusEffect> statusEffects) {
            this.statusEffects = statusEffects;
        }
        
        public boolean isElementRemoval() {
            return elementRemoval;
        }
        
        public void setElementRemoval(boolean elementRemoval) {
            this.elementRemoval = elementRemoval;
        }
        
        public int getCooldown() {
            return cooldown;
        }
        
        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }
        
        /**
         * 获取所有效果类型
         */
        public List<String> getAllEffectTypes() {
            List<String> types = new java.util.ArrayList<>();
            
            if (reactionEffects != null) {
                for (ReactionEffect effect : reactionEffects) {
                    if (effect.getEffectType() != null && !effect.getEffectType().isEmpty()) {
                        types.add(effect.getEffectType());
                    }
                }
            }
            
            return types;
        }
        
        /**
         * 检查是否有配置效果
         */
        public boolean hasEffects() {
            return reactionEffects != null && !reactionEffects.isEmpty();
        }
        
        /**
         * 获取主要效果类型
         */
        public String getPrimaryEffectType() {
            if (hasEffects()) {
                return reactionEffects.get(0).getEffectType();
            }
            return null;
        }
        
        /**
         * 获取主要伤害倍率
         */
        public float getPrimaryDamageMultiplier() {
            if (hasEffects()) {
                return reactionEffects.get(0).getDamageMultiplier();
            }
            return 1.0f;
        }
    }
    
    /**
     * 单个反应效果配置
     */
    public static class ReactionEffect {
        @SerializedName("effect_type")
        private String effectType;
        
        @SerializedName("damage_multiplier")
        private float damageMultiplier = 1.0f;
        
        @SerializedName("area_radius")
        private float areaRadius = 0.0f;
        
        @SerializedName("include_self")
        private boolean includeSelf = false;
        
        @SerializedName("damage_source")
        private String damageSource;
        
        @SerializedName("priority")
        private int priority = 0;
        
        @SerializedName("conditions")
        private EffectConditions conditions;
        
        // Getters and Setters
        public String getEffectType() {
            return effectType;
        }
        
        public void setEffectType(String effectType) {
            this.effectType = effectType;
        }
        
        public float getDamageMultiplier() {
            return damageMultiplier;
        }
        
        public void setDamageMultiplier(float damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
        }
        
        public float getAreaRadius() {
            return areaRadius;
        }
        
        public void setAreaRadius(float areaRadius) {
            this.areaRadius = areaRadius;
        }
        
        public boolean isIncludeSelf() {
            return includeSelf;
        }
        
        public void setIncludeSelf(boolean includeSelf) {
            this.includeSelf = includeSelf;
        }
        
        public String getDamageSource() {
            return damageSource;
        }
        
        public void setDamageSource(String damageSource) {
            this.damageSource = damageSource;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        public EffectConditions getConditions() {
            return conditions;
        }
        
        public void setConditions(EffectConditions conditions) {
            this.conditions = conditions;
        }
    }
    
    /**
     * 效果触发条件
     */
    public static class EffectConditions {
        @SerializedName("minimum_damage")
        private float minimumDamage = 0.0f;
        
        @SerializedName("maximum_damage")
        private float maximumDamage = Float.MAX_VALUE;
        
        @SerializedName("required_elements")
        private List<String> requiredElements;
        
        @SerializedName("excluded_elements")
        private List<String> excludedElements;
        
        // Getters and Setters
        public float getMinimumDamage() {
            return minimumDamage;
        }
        
        public void setMinimumDamage(float minimumDamage) {
            this.minimumDamage = minimumDamage;
        }
        
        public float getMaximumDamage() {
            return maximumDamage;
        }
        
        public void setMaximumDamage(float maximumDamage) {
            this.maximumDamage = maximumDamage;
        }
        
        public List<String> getRequiredElements() {
            return requiredElements;
        }
        
        public void setRequiredElements(List<String> requiredElements) {
            this.requiredElements = requiredElements;
        }
        
        public List<String> getExcludedElements() {
            return excludedElements;
        }
        
        public void setExcludedElements(List<String> excludedElements) {
            this.excludedElements = excludedElements;
        }
    }
    
    /**
     * 状态效果配置
     */
    public static class StatusEffect {
        @SerializedName("effect_id")
        private String effectId;
        
        @SerializedName("duration")
        private int duration;
        
        @SerializedName("amplifier")
        private int amplifier;
        
        // Getters and Setters
        public String getEffectId() {
            return effectId;
        }
        
        public void setEffectId(String effectId) {
            this.effectId = effectId;
        }
        
        public int getDuration() {
            return duration;
        }
        
        public void setDuration(int duration) {
            this.duration = duration;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public void setAmplifier(int amplifier) {
            this.amplifier = amplifier;
        }
    }
    
    /**
     * 反应触发条件
     */
    public static class ReactionConditions {
        @SerializedName("minimum_primary_amount")
        private int minimumPrimaryAmount;
        
        @SerializedName("minimum_secondary_amount")
        private int minimumSecondaryAmount;
        
        @SerializedName("trigger_delay")
        private int triggerDelay;
        
        @SerializedName("environmental_requirements")
        private List<String> environmentalRequirements;
        
        @SerializedName("excluded_conditions")
        private List<String> excludedConditions;
        
        // Getters and Setters
        public int getMinimumPrimaryAmount() {
            return minimumPrimaryAmount;
        }
        
        public void setMinimumPrimaryAmount(int minimumPrimaryAmount) {
            this.minimumPrimaryAmount = minimumPrimaryAmount;
        }
        
        public int getMinimumSecondaryAmount() {
            return minimumSecondaryAmount;
        }
        
        public void setMinimumSecondaryAmount(int minimumSecondaryAmount) {
            this.minimumSecondaryAmount = minimumSecondaryAmount;
        }
        
        public int getTriggerDelay() {
            return triggerDelay;
        }
        
        public void setTriggerDelay(int triggerDelay) {
            this.triggerDelay = triggerDelay;
        }
        
        public List<String> getEnvironmentalRequirements() {
            return environmentalRequirements;
        }
        
        public void setEnvironmentalRequirements(List<String> environmentalRequirements) {
            this.environmentalRequirements = environmentalRequirements;
        }
        
        public List<String> getExcludedConditions() {
            return excludedConditions;
        }
        
        public void setExcludedConditions(List<String> excludedConditions) {
            this.excludedConditions = excludedConditions;
        }
    }
    
    /**
     * 视觉效果配置
     */
    public static class VisualEffects {
        @SerializedName("screen_effect")
        private String screenEffect;
        
        @SerializedName("color_overlay")
        private String colorOverlay;
        
        @SerializedName("animation_duration")
        private int animationDuration;
        
        @SerializedName("fade_in_out")
        private boolean fadeInOut;
        
        // Getters and Setters
        public String getScreenEffect() {
            return screenEffect;
        }
        
        public void setScreenEffect(String screenEffect) {
            this.screenEffect = screenEffect;
        }
        
        public String getColorOverlay() {
            return colorOverlay;
        }
        
        public void setColorOverlay(String colorOverlay) {
            this.colorOverlay = colorOverlay;
        }
        
        public int getAnimationDuration() {
            return animationDuration;
        }
        
        public void setAnimationDuration(int animationDuration) {
            this.animationDuration = animationDuration;
        }
        
        public boolean isFadeInOut() {
            return fadeInOut;
        }
        
        public void setFadeInOut(boolean fadeInOut) {
            this.fadeInOut = fadeInOut;
        }
    }
    
    /**
     * 音效配置
     */
    public static class SoundEffects {
        @SerializedName("reaction_sound")
        private String reactionSound;
        
        @SerializedName("volume")
        private float volume;
        
        @SerializedName("pitch")
        private float pitch;
        
        @SerializedName("attenuation_distance")
        private float attenuationDistance;
        
        // Getters and Setters
        public String getReactionSound() {
            return reactionSound;
        }
        
        public void setReactionSound(String reactionSound) {
            this.reactionSound = reactionSound;
        }
        
        public float getVolume() {
            return volume;
        }
        
        public void setVolume(float volume) {
            this.volume = volume;
        }
        
        public float getPitch() {
            return pitch;
        }
        
        public void setPitch(float pitch) {
            this.pitch = pitch;
        }
        
        public float getAttenuationDistance() {
            return attenuationDistance;
        }
        
        public void setAttenuationDistance(float attenuationDistance) {
            this.attenuationDistance = attenuationDistance;
        }
    }
    
    /**
     * 粒子效果配置
     */
    public static class ParticleEffects {
        @SerializedName("particle_type")
        private String particleType;
        
        @SerializedName("particle_count")
        private int particleCount;
        
        @SerializedName("spread_radius")
        private float spreadRadius;
        
        @SerializedName("particle_speed")
        private float particleSpeed;
        
        @SerializedName("particle_lifetime")
        private int particleLifetime;
        
        // Getters and Setters
        public String getParticleType() {
            return particleType;
        }
        
        public void setParticleType(String particleType) {
            this.particleType = particleType;
        }
        
        public int getParticleCount() {
            return particleCount;
        }
        
        public void setParticleCount(int particleCount) {
            this.particleCount = particleCount;
        }
        
        public float getSpreadRadius() {
            return spreadRadius;
        }
        
        public void setSpreadRadius(float spreadRadius) {
            this.spreadRadius = spreadRadius;
        }
        
        public float getParticleSpeed() {
            return particleSpeed;
        }
        
        public void setParticleSpeed(float particleSpeed) {
            this.particleSpeed = particleSpeed;
        }
        
        public int getParticleLifetime() {
            return particleLifetime;
        }
        
        public void setParticleLifetime(int particleLifetime) {
            this.particleLifetime = particleLifetime;
        }
    }
} 