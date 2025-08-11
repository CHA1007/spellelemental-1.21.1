package com.chadate.spellelemental.element.attachment.environmental.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 环境条件元素附着配置类
 * 定义基于环境条件的元素附着规则
 */
public class EnvironmentalAttachmentConfig {
    
    @SerializedName("element_id")
    public String elementId;
    
    @SerializedName("display_name")
    public String displayName;
    
    @SerializedName("attachment_type")
    public String attachmentType;
    
    @SerializedName("environmental_conditions")
    public EnvironmentalConditions environmentalConditions;
    
    @SerializedName("effects")
    public EffectConfig effects;
    
    @SerializedName("visual")
    public VisualConfig visual;
    
    public static class EnvironmentalConditions {
        @SerializedName("water_conditions")
        public WaterConditions waterConditions;
        
        @SerializedName("weather_conditions")
        public WeatherConditions weatherConditions;
        
        @SerializedName("biome_conditions")
        public BiomeConditions biomeConditions;
        
        @SerializedName("block_conditions")
        public BlockConditions blockConditions;
        
        @SerializedName("time_conditions")
        public TimeConditions timeConditions;
        
        @SerializedName("check_interval")
        public int checkInterval = 20; // 检查间隔（tick）
    }
    
    public static class WaterConditions {
        @SerializedName("in_water")
        public boolean inWater = false;
        
        @SerializedName("in_water_or_rain")
        public boolean inWaterOrRain = false;
        
        @SerializedName("touching_water")
        public boolean touchingWater = false;
    }
    
    public static class WeatherConditions {
        @SerializedName("raining")
        public boolean raining = false;
        
        @SerializedName("thundering")
        public boolean thundering = false;
        
        @SerializedName("clear_sky")
        public boolean clearSky = false;
    }
    
    public static class BiomeConditions {
        @SerializedName("biome_tags")
        public List<String> biomeTags;
        
        @SerializedName("biome_ids")
        public List<String> biomeIds;
        
        @SerializedName("temperature_range")
        public TemperatureRange temperatureRange;
    }
    
    public static class TemperatureRange {
        @SerializedName("min_temperature")
        public float minTemperature = Float.NEGATIVE_INFINITY;
        
        @SerializedName("max_temperature")
        public float maxTemperature = Float.POSITIVE_INFINITY;
    }
    
    public static class BlockConditions {
        @SerializedName("nearby_blocks")
        public List<String> nearbyBlocks;
        
        @SerializedName("standing_on_blocks")
        public List<String> standingOnBlocks;
        
        @SerializedName("search_radius")
        public int searchRadius = 3;
    }
    
    public static class TimeConditions {
        @SerializedName("day_time")
        public boolean dayTime = false;
        
        @SerializedName("night_time")
        public boolean nightTime = false;
        
        @SerializedName("time_range")
        public TimeRange timeRange;
    }
    
    public static class TimeRange {
        @SerializedName("start_time")
        public long startTime = 0;
        
        @SerializedName("end_time")
        public long endTime = 24000;
    }
    
    public static class EffectConfig {
        @SerializedName("duration")
        public int duration = 200;
        
        @SerializedName("stack_limit")
        public int stackLimit = 10;
        
        @SerializedName("decay_rate")
        public int decayRate = 1;
        
        @SerializedName("network_sync")
        public boolean networkSync = true;
        
        @SerializedName("refresh_on_trigger")
        public boolean refreshOnTrigger = true;
    }
    
    public static class VisualConfig {
        @SerializedName("icon")
        public String icon;
        
        @SerializedName("color")
        public String color;
        
        @SerializedName("particle_effect")
        public String particleEffect;
    }
}
