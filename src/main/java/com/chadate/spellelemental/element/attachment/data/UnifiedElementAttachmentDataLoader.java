package com.chadate.spellelemental.element.attachment.data;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;
import com.chadate.spellelemental.element.attachment.attack.ElementAttachmentRegistry;
import com.chadate.spellelemental.element.attachment.attack.DynamicElementHandler;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * 统一的元素附着数据加载器
 * 支持基于伤害源和基于环境条件的元素附着配置
 * 通过配置中的 type 字段自动分发到对应的处理器
 */
public class UnifiedElementAttachmentDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    
    public UnifiedElementAttachmentDataLoader() {
        super(GSON, "element_attachments");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, 
                        ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        
        // 清空现有的处理器与图标映射
        ElementAttachmentRegistry.clearHandlers();
        UnifiedElementAttachmentAssets.clear();
        EnvironmentalAttachmentRegistry.clear();
        
        SpellElemental.LOGGER.info("开始加载统一元素附着配置...");
        
        int damageSourceCount = 0;
        int environmentalCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            
            try {
                // 解析配置
                UnifiedElementAttachmentConfig config = GSON.fromJson(jsonElement, UnifiedElementAttachmentConfig.class);
                
                // 验证配置
                if (!validateConfig(config, resourceLocation)) {
                    errorCount++;
                    continue;
                }
                
                // 记录图标（如果提供）
                if (config.getVisual() != null && config.getVisual().getIcon() != null) {
                    UnifiedElementAttachmentAssets.setIcon(config.getAttachmentType(), config.getVisual().getIcon());
                }
                
                // 根据类型分发到对应的处理器
                if (config.isDamageSourceType()) {
                    registerDamageSourceHandler(config, resourceLocation);
                    damageSourceCount++;
                } else if (config.isEnvironmentalType()) {
                    registerEnvironmentalHandler(config, resourceLocation);
                    EnvironmentalAttachmentRegistry.add(config);
                    environmentalCount++;
                } else {
                    SpellElemental.LOGGER.error("未知的附着类型: {} in {}", config.getType(), resourceLocation);
                    errorCount++;
                }
                
            } catch (JsonParseException e) {
                SpellElemental.LOGGER.error("解析元素附着配置失败: {} - {}", resourceLocation, e.getMessage());
                errorCount++;
            } catch (Exception e) {
                SpellElemental.LOGGER.error("加载元素附着配置时发生错误: {} - {}", resourceLocation, e.getMessage(), e);
                errorCount++;
            }
        }
        
        SpellElemental.LOGGER.info("统一元素附着配置加载完成: {} 个伤害源附着, {} 个环境附着, {} 个错误", 
                                 damageSourceCount, environmentalCount, errorCount);
    }
    
    /**
     * 验证配置的有效性
     */
    private boolean validateConfig(UnifiedElementAttachmentConfig config, ResourceLocation resourceLocation) {
        if (config == null) {
            SpellElemental.LOGGER.error("配置为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getElementId() == null || config.getElementId().trim().isEmpty()) {
            SpellElemental.LOGGER.error("元素ID不能为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getAttachmentType() == null || config.getAttachmentType().trim().isEmpty()) {
            SpellElemental.LOGGER.error("附着类型不能为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            SpellElemental.LOGGER.error("触发类型不能为空: {}", resourceLocation);
            return false;
        }
        
        // 验证类型特定的条件
        if (config.isDamageSourceType()) {
            if (config.getDamageSourceConditions() == null) {
                SpellElemental.LOGGER.error("伤害源类型配置缺少 damage_source_conditions: {}", resourceLocation);
                return false;
            }
            
            if (config.getDamageSourceConditions().getDamageSourcePatterns() == null || 
                config.getDamageSourceConditions().getDamageSourcePatterns().isEmpty()) {
                SpellElemental.LOGGER.error("伤害源模式不能为空: {}", resourceLocation);
                return false;
            }
        } else if (config.isEnvironmentalType()) {
            if (config.getEnvironmentalConditions() == null) {
                SpellElemental.LOGGER.error("环境类型配置缺少 environmental_conditions: {}", resourceLocation);
                return false;
            }
        }
        
        if (config.getEffects() == null) {
            SpellElemental.LOGGER.error("效果配置不能为空: {}", resourceLocation);
            return false;
        }
        
        return true;
    }
    
    /**
     * 注册基于伤害源的附着处理器
     */
    private void registerDamageSourceHandler(UnifiedElementAttachmentConfig config, ResourceLocation resourceLocation) {
        try {
            DynamicElementHandler handler = new DynamicElementHandler(config);
            ElementAttachmentRegistry.register(handler);
            
            SpellElemental.LOGGER.debug("注册伤害源元素附着处理器: {} -> {}", 
                                      config.getElementId(), config.getAttachmentType());
            
        } catch (Exception e) {
            SpellElemental.LOGGER.error("注册伤害源附着处理器失败: {} - {}", resourceLocation, e.getMessage(), e);
        }
    }
    
    /**
     * 注册基于环境条件的附着处理器
     * 注意：环境条件附着暂时通过现有的元素环境系统处理
     * 这里仅记录配置，实际处理由 ElementsEnvironment 类负责
     */
    private void registerEnvironmentalHandler(UnifiedElementAttachmentConfig config, ResourceLocation resourceLocation) {
        try {
            SpellElemental.LOGGER.info("环境条件元素附着配置已加载: {} -> {}", 
                                     config.getElementId(), config.getAttachmentType());
            SpellElemental.LOGGER.info("环境条件将由现有的 ElementsEnvironment 系统处理");
            
        } catch (Exception e) {
            SpellElemental.LOGGER.error("加载环境附着配置失败: {} - {}", resourceLocation, e.getMessage(), e);
        }
    }
}
