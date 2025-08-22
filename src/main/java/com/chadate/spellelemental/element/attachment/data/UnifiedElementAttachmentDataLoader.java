package com.chadate.spellelemental.element.attachment.data;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

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
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {

        // 仅清理与资产映射相关的数据
        UnifiedElementAttachmentAssets.clear();

        SpellElemental.LOGGER.info("开始加载元素附着资产配置(最小化)...");

        int loadedCount = 0;
        int errorCount = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                UnifiedElementAttachmentConfig config = GSON.fromJson(json, UnifiedElementAttachmentConfig.class);
                if (config == null || config.getElementId() == null || config.getElementId().isBlank()) {
                    SpellElemental.LOGGER.error("元素附着配置缺少 element_id: {}", id);
                    errorCount++;
                    continue;
                }

                String elementId = config.getElementId();
                if (config.getVisual() != null) {
                    if (config.getVisual().getIcon() != null && !config.getVisual().getIcon().isBlank()) {
                        UnifiedElementAttachmentAssets.setIcon(elementId, config.getVisual().getIcon());
                    }
                    if (config.getVisual().getParticleEffect() != null && !config.getVisual().getParticleEffect().isBlank()) {
                        UnifiedElementAttachmentAssets.setParticleEffect(elementId, config.getVisual().getParticleEffect());
                    }
                }
                if (config.getSchool() != null && !config.getSchool().isBlank()) {
                    UnifiedElementAttachmentAssets.setSchool(elementId, config.getSchool());
                }

                loadedCount++;
            } catch (JsonParseException e) {
                SpellElemental.LOGGER.error("解析元素附着配置失败: {} - {}", id, e.getMessage());
                errorCount++;
            } catch (Exception e) {
                SpellElemental.LOGGER.error("加载元素附着配置时发生错误: {} - {}", id, e.getMessage(), e);
                errorCount++;
            }
        }

        SpellElemental.LOGGER.info("元素附着资产加载完成: {} 条成功, {} 条错误", loadedCount, errorCount);
    }
}
