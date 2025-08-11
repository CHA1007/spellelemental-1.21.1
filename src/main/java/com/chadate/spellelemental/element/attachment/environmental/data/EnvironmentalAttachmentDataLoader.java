package com.chadate.spellelemental.element.attachment.environmental.data;

import com.chadate.spellelemental.element.attachment.environmental.EnvironmentalAttachmentRegistry;
import com.chadate.spellelemental.element.attachment.environmental.config.EnvironmentalAttachmentConfig;
import com.chadate.spellelemental.element.attachment.environmental.handler.DynamicEnvironmentalHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 环境条件元素附着数据加载器
 * 负责从数据包中加载环境条件配置并注册处理器
 */
public class EnvironmentalAttachmentDataLoader extends SimpleJsonResourceReloadListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentalAttachmentDataLoader.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public EnvironmentalAttachmentDataLoader() {
        super(GSON, "environmental_attachments");
    }
    
    @Override
    protected void apply(@Nonnull Map<ResourceLocation, com.google.gson.JsonElement> resourceLocationJsonElementMap, 
                        @Nonnull ResourceManager resourceManager, 
                        @Nonnull ProfilerFiller profilerFiller) {
        
        LOGGER.info("Loading environmental attachment configurations...");
        
        // 清空现有处理器
        EnvironmentalAttachmentRegistry.clearHandlers();
        
        int loadedCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<ResourceLocation, com.google.gson.JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            com.google.gson.JsonElement jsonElement = entry.getValue();
            
            try {
                LOGGER.debug("Loading environmental attachment config: {}", resourceLocation);
                
                // 解析JSON配置
                EnvironmentalAttachmentConfig config = GSON.fromJson(jsonElement, EnvironmentalAttachmentConfig.class);
                
                // 验证配置
                if (!validateConfig(config, resourceLocation)) {
                    errorCount++;
                    continue;
                }
                
                // 创建并注册动态处理器
                DynamicEnvironmentalHandler handler = new DynamicEnvironmentalHandler(config);
                EnvironmentalAttachmentRegistry.register(handler);
                
                loadedCount++;
                LOGGER.debug("Successfully loaded environmental attachment: {} ({})", 
                    config.elementId, config.displayName);
                
            } catch (Exception e) {
                errorCount++;
                LOGGER.error("Failed to load environmental attachment config: {}", resourceLocation, e);
            }
        }
        
        LOGGER.info("Environmental attachment loading complete. Loaded: {}, Errors: {}, Total handlers: {}", 
            loadedCount, errorCount, EnvironmentalAttachmentRegistry.getHandlerCount());
        
        if (loadedCount > 0) {
            LOGGER.info("Registered environmental elements: {}", 
                EnvironmentalAttachmentRegistry.getRegisteredElementIds());
        }
    }
    
    private boolean validateConfig(EnvironmentalAttachmentConfig config, ResourceLocation resourceLocation) {
        if (config == null) {
            LOGGER.error("Environmental attachment config is null: {}", resourceLocation);
            return false;
        }
        
        if (config.elementId == null || config.elementId.isEmpty()) {
            LOGGER.error("Environmental attachment config missing element_id: {}", resourceLocation);
            return false;
        }
        
        if (config.attachmentType == null || config.attachmentType.isEmpty()) {
            LOGGER.error("Environmental attachment config missing attachment_type: {}", resourceLocation);
            return false;
        }
        
        if (config.environmentalConditions == null) {
            LOGGER.error("Environmental attachment config missing environmental_conditions: {}", resourceLocation);
            return false;
        }
        
        // 验证至少有一个环境条件被配置
        boolean hasCondition = false;
        var conditions = config.environmentalConditions;
        
        if (conditions.waterConditions != null && 
            (conditions.waterConditions.inWater || conditions.waterConditions.inWaterOrRain || conditions.waterConditions.touchingWater)) {
            hasCondition = true;
        }
        
        if (conditions.weatherConditions != null && 
            (conditions.weatherConditions.raining || conditions.weatherConditions.thundering || conditions.weatherConditions.clearSky)) {
            hasCondition = true;
        }
        
        if (conditions.biomeConditions != null && 
            ((conditions.biomeConditions.biomeTags != null && !conditions.biomeConditions.biomeTags.isEmpty()) ||
             (conditions.biomeConditions.biomeIds != null && !conditions.biomeConditions.biomeIds.isEmpty()) ||
             conditions.biomeConditions.temperatureRange != null)) {
            hasCondition = true;
        }
        
        if (conditions.blockConditions != null && 
            ((conditions.blockConditions.nearbyBlocks != null && !conditions.blockConditions.nearbyBlocks.isEmpty()) ||
             (conditions.blockConditions.standingOnBlocks != null && !conditions.blockConditions.standingOnBlocks.isEmpty()))) {
            hasCondition = true;
        }
        
        if (conditions.timeConditions != null && 
            (conditions.timeConditions.dayTime || conditions.timeConditions.nightTime || conditions.timeConditions.timeRange != null)) {
            hasCondition = true;
        }
        
        if (!hasCondition) {
            LOGGER.error("Environmental attachment config has no valid conditions: {}", resourceLocation);
            return false;
        }
        
        LOGGER.debug("Environmental attachment config validation passed: {}", resourceLocation);
        return true;
    }
}
