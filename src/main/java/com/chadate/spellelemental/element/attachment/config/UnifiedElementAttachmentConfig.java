package com.chadate.spellelemental.element.attachment.config;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 统一的元素附着配置类
 * 支持基于伤害源和基于环境条件的元素附着
 * 通过 type 字段区分附着类型
 */
public class UnifiedElementAttachmentConfig {
    
    @SerializedName("element_id")
    private String elementId;
    
    @SerializedName("display_name")
    private String displayName;
    
    @SerializedName("attachment_type")
    private String attachmentType;
    
    /**
     * 附着触发类型：
     * - "damage_source": 基于伤害源的附着
     * - "environmental": 基于环境条件的附着
     */
    @SerializedName("type")
    private String type;
    
    // 基于伤害源的触发条件（当 type = "damage_source" 时使用）
    @SerializedName("damage_source_conditions")
    private DamageSourceConditions damageSourceConditions;
    
    // 基于环境条件的触发条件（当 type = "environmental" 时使用）
    @SerializedName("environmental_conditions")
    private EnvironmentalConditions environmentalConditions;
    
    @SerializedName("effects")
    private EffectConfig effects;
    
    @SerializedName("visual")
    private VisualConfig visual;
    
    // 构造函数
    public UnifiedElementAttachmentConfig() {}
    
    public UnifiedElementAttachmentConfig(String elementId, String displayName, String attachmentType, String type) {
        this.elementId = elementId;
        this.displayName = displayName;
        this.attachmentType = attachmentType;
        this.type = type;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public DamageSourceConditions getDamageSourceConditions() {
        return damageSourceConditions;
    }
    
    public void setDamageSourceConditions(DamageSourceConditions damageSourceConditions) {
        this.damageSourceConditions = damageSourceConditions;
    }
    
    public EnvironmentalConditions getEnvironmentalConditions() {
        return environmentalConditions;
    }
    
    public void setEnvironmentalConditions(EnvironmentalConditions environmentalConditions) {
        this.environmentalConditions = environmentalConditions;
    }
    
    public EffectConfig getEffects() {
        return effects;
    }
    
    public void setEffects(EffectConfig effects) {
        this.effects = effects;
    }
    
    public VisualConfig getVisual() {
        return visual;
    }
    
    public void setVisual(VisualConfig visual) {
        this.visual = visual;
    }
    
    /**
     * 检查是否为基于伤害源的附着
     */
    public boolean isDamageSourceType() {
        return "damage_source".equals(type);
    }
    
    /**
     * 检查是否为基于环境条件的附着
     */
    public boolean isEnvironmentalType() {
        return "environmental".equals(type);
    }
    
    /**
     * 基于伤害源的触发条件（已简化）
     */
    public static class DamageSourceConditions {
        @SerializedName("damage_source_patterns")
        private List<String> damageSourcePatterns;
        
        public List<String> getDamageSourcePatterns() {
            return damageSourcePatterns;
        }
        
        public void setDamageSourcePatterns(List<String> damageSourcePatterns) {
            this.damageSourcePatterns = damageSourcePatterns;
        }
    }
    
    /**
     * 基于环境条件的触发条件
     */
    public static class EnvironmentalConditions {
        @SerializedName("water_conditions")
        private WaterConditions waterConditions;
        
        @SerializedName("weather_conditions")
        private WeatherConditions weatherConditions;
        
        @SerializedName("biome_conditions")
        private BiomeConditions biomeConditions;
        
        @SerializedName("block_conditions")
        private BlockConditions blockConditions;
        
        @SerializedName("time_conditions")
        private TimeConditions timeConditions;
        
        @SerializedName("check_interval")
        private int checkInterval = 20; // 检查间隔（tick）
        
        // Getters and Setters
        public WaterConditions getWaterConditions() {
            return waterConditions;
        }
        
        public void setWaterConditions(WaterConditions waterConditions) {
            this.waterConditions = waterConditions;
        }
        
        public WeatherConditions getWeatherConditions() {
            return weatherConditions;
        }
        
        public void setWeatherConditions(WeatherConditions weatherConditions) {
            this.weatherConditions = weatherConditions;
        }
        
        public BiomeConditions getBiomeConditions() {
            return biomeConditions;
        }
        
        public void setBiomeConditions(BiomeConditions biomeConditions) {
            this.biomeConditions = biomeConditions;
        }
        
        public BlockConditions getBlockConditions() {
            return blockConditions;
        }
        
        public void setBlockConditions(BlockConditions blockConditions) {
            this.blockConditions = blockConditions;
        }
        
        public TimeConditions getTimeConditions() {
            return timeConditions;
        }
        
        public void setTimeConditions(TimeConditions timeConditions) {
            this.timeConditions = timeConditions;
        }
        
        public int getCheckInterval() {
            return checkInterval;
        }
        
        public void setCheckInterval(int checkInterval) {
            this.checkInterval = checkInterval;
        }
    }
    
    /**
     * 水环境条件
     */
    public static class WaterConditions {
        @SerializedName("in_water")
        private boolean inWater = false;
        
        @SerializedName("in_rain")
        private boolean inRain = false;
        
        @SerializedName("touching_water")
        private boolean touchingWater = false;
        
        @SerializedName("underwater")
        private boolean underwater = false;
        
        // Getters and Setters
        public boolean isInWater() {
            return inWater;
        }
        
        public void setInWater(boolean inWater) {
            this.inWater = inWater;
        }
        
        public boolean isInRain() {
            return inRain;
        }
        
        public void setInRain(boolean inRain) {
            this.inRain = inRain;
        }
        
        public boolean isTouchingWater() {
            return touchingWater;
        }
        
        public void setTouchingWater(boolean touchingWater) {
            this.touchingWater = touchingWater;
        }
        
        public boolean isUnderwater() {
            return underwater;
        }
        
        public void setUnderwater(boolean underwater) {
            this.underwater = underwater;
        }
    }
    
    /**
     * 天气条件
     */
    public static class WeatherConditions {
        @SerializedName("weather_types")
        private List<String> weatherTypes;
        
        @SerializedName("thunder")
        private Boolean thunder;
        
        @SerializedName("rain")
        private Boolean rain;
        
        // Getters and Setters
        public List<String> getWeatherTypes() {
            return weatherTypes;
        }
        
        public void setWeatherTypes(List<String> weatherTypes) {
            this.weatherTypes = weatherTypes;
        }
        
        public Boolean getThunder() {
            return thunder;
        }
        
        public void setThunder(Boolean thunder) {
            this.thunder = thunder;
        }
        
        public Boolean getRain() {
            return rain;
        }
        
        public void setRain(Boolean rain) {
            this.rain = rain;
        }
    }
    
    /**
     * 生物群系条件
     */
    public static class BiomeConditions {
        @SerializedName("biome_patterns")
        private List<String> biomePatterns;
        
        @SerializedName("biome_tags")
        private List<String> biomeTags;
        
        // Getters and Setters
        public List<String> getBiomePatterns() {
            return biomePatterns;
        }
        
        public void setBiomePatterns(List<String> biomePatterns) {
            this.biomePatterns = biomePatterns;
        }
        
        public List<String> getBiomeTags() {
            return biomeTags;
        }
        
        public void setBiomeTags(List<String> biomeTags) {
            this.biomeTags = biomeTags;
        }
    }
    
    /**
     * 方块条件
     */
    public static class BlockConditions {
        @SerializedName("standing_on_blocks")
        private List<String> standingOnBlocks;
        
        @SerializedName("surrounded_by_blocks")
        private List<String> surroundedByBlocks;
        
        @SerializedName("nearby_blocks")
        private List<String> nearbyBlocks;
        
        @SerializedName("check_radius")
        private int checkRadius = 3;
        
        // Getters and Setters
        public List<String> getStandingOnBlocks() {
            return standingOnBlocks;
        }
        
        public void setStandingOnBlocks(List<String> standingOnBlocks) {
            this.standingOnBlocks = standingOnBlocks;
        }
        
        public List<String> getSurroundedByBlocks() {
            return surroundedByBlocks;
        }
        
        public void setSurroundedByBlocks(List<String> surroundedByBlocks) {
            this.surroundedByBlocks = surroundedByBlocks;
        }
        
        public List<String> getNearbyBlocks() {
            return nearbyBlocks;
        }
        
        public void setNearbyBlocks(List<String> nearbyBlocks) {
            this.nearbyBlocks = nearbyBlocks;
        }
        
        public int getCheckRadius() {
            return checkRadius;
        }
        
        public void setCheckRadius(int checkRadius) {
            this.checkRadius = checkRadius;
        }
    }
    
    /**
     * 时间条件
     */
    public static class TimeConditions {
        @SerializedName("time_range")
        private TimeRange timeRange;
        
        @SerializedName("moon_phases")
        private List<String> moonPhases;
        
        // Getters and Setters
        public TimeRange getTimeRange() {
            return timeRange;
        }
        
        public void setTimeRange(TimeRange timeRange) {
            this.timeRange = timeRange;
        }
        
        public List<String> getMoonPhases() {
            return moonPhases;
        }
        
        public void setMoonPhases(List<String> moonPhases) {
            this.moonPhases = moonPhases;
        }
        
        public static class TimeRange {
            @SerializedName("start_time")
            private int startTime;
            
            @SerializedName("end_time")
            private int endTime;
            
            // Getters and Setters
            public int getStartTime() {
                return startTime;
            }
            
            public void setStartTime(int startTime) {
                this.startTime = startTime;
            }
            
            public int getEndTime() {
                return endTime;
            }
            
            public void setEndTime(int endTime) {
                this.endTime = endTime;
            }
        }
    }
    
    /**
     * 效果配置（已简化）
     */
    public static class EffectConfig {
        @SerializedName("duration")
        private int duration;
        
        public int getDuration() {
            return duration;
        }
        
        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
    
    /**
     * 视觉效果配置（已简化）
     */
    public static class VisualConfig {
        @SerializedName("particle_effect")
        private String particleEffect;
        
        @SerializedName("sound_effect")
        private String soundEffect;
        
        @SerializedName("icon")
        private String icon;
        
        public String getParticleEffect() {
            return particleEffect;
        }
        
        public void setParticleEffect(String particleEffect) {
            this.particleEffect = particleEffect;
        }
        
        public String getSoundEffect() {
            return soundEffect;
        }
        
        public void setSoundEffect(String soundEffect) {
            this.soundEffect = soundEffect;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
