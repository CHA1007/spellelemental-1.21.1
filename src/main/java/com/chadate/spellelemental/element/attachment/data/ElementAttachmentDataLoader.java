package com.chadate.spellelemental.element.attachment.data;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.element.attachment.attack.DynamicElementHandler;
import com.chadate.spellelemental.element.attachment.attack.ElementAttachmentRegistry;
import com.chadate.spellelemental.element.attachment.config.ElementAttachmentConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素附着数据包加载器
 * 负责从 data/spellelemental/element_attachments/ 目录加载 JSON 配置
 */
public class ElementAttachmentDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private static final Map<ResourceLocation, ElementAttachmentConfig> CONFIGS = new HashMap<>();
    
    public ElementAttachmentDataLoader() {
        super(GSON, "element_attachments");
    }

    @Override
    protected void apply(@javax.annotation.Nonnull Map<ResourceLocation, JsonElement> map, @javax.annotation.Nonnull ResourceManager resourceManager, @javax.annotation.Nonnull ProfilerFiller profiler) {
        SpellElemental.LOGGER.info("Loading element attachment configurations...");
        
        // 清空现有配置和注册
        CONFIGS.clear();
        ElementAttachmentRegistry.clearHandlers();
        
        int loadedCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();
            
            try {
                SpellElemental.LOGGER.info("Processing config file: {}", location);
                SpellElemental.LOGGER.info("Raw JSON: {}", json.toString());
                
                ElementAttachmentConfig config = GSON.fromJson(json, ElementAttachmentConfig.class);
                if (config != null) {
                    SpellElemental.LOGGER.info("Parsed config - element_id: {}, attachment_type: {}", 
                        config.getElementId(), config.getAttachmentType());
                } else {
                    SpellElemental.LOGGER.warn("Failed to parse config, result is null");
                }
                
                // 验证配置
                if (validateConfig(config, location)) {
                    CONFIGS.put(location, config);
                    
                    // 动态注册处理器
                    DynamicElementHandler handler = new DynamicElementHandler(config);
                    ElementAttachmentRegistry.register(handler);
                    
                    loadedCount++;
                    SpellElemental.LOGGER.debug("Loaded element attachment config: {} -> {}", 
                        location, config.getElementId());
                } else {
                    errorCount++;
                }
                
            } catch (Exception e) {
                errorCount++;
                SpellElemental.LOGGER.error("Failed to load element attachment config: {}", location, e);
            }
        }
        
        SpellElemental.LOGGER.info("Element attachment loading complete. Loaded: {}, Errors: {}", 
            loadedCount, errorCount);
    }
    
    /**
     * 验证配置完整性
     */
    private boolean validateConfig(ElementAttachmentConfig config, ResourceLocation location) {
        if (config == null) {
            SpellElemental.LOGGER.error("Config is null for: {}", location);
            return false;
        }
        
        if (config.getElementId() == null || config.getElementId().isEmpty()) {
            SpellElemental.LOGGER.error("Missing element_id in config: {}", location);
            return false;
        }
        
        if (config.getAttachmentType() == null || config.getAttachmentType().isEmpty()) {
            SpellElemental.LOGGER.error("Missing attachment_type in config: {}", location);
            return false;
        }
        
        if (config.getTriggerConditions() == null || 
            config.getTriggerConditions().getDamageSourcePatterns() == null ||
            config.getTriggerConditions().getDamageSourcePatterns().isEmpty()) {
            SpellElemental.LOGGER.error("Missing or empty trigger conditions in config: {}", location);
            return false;
        }
        
        // 设置默认值
        if (config.getEffects() == null) {
            config.setEffects(new ElementAttachmentConfig.EffectProperties());
        }
        
        if (config.getDisplayName() == null) {
            config.setDisplayName(config.getElementId());
        }
        
        return true;
    }
    
    /**
     * 获取所有已加载的配置
     */
    public static Map<ResourceLocation, ElementAttachmentConfig> getConfigs() {
        return new HashMap<>(CONFIGS);
    }
    
    /**
     * 根据元素ID获取配置
     */
    public static ElementAttachmentConfig getConfigByElementId(String elementId) {
        return CONFIGS.values().stream()
                .filter(config -> elementId.equals(config.getElementId()))
                .findFirst()
                .orElse(null);
    }
}
