package com.chadate.spellelemental.element.reaction.data;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.element.reaction.config.ElementReactionConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 元素反应数据加载器
 * 负责从 JSON 数据包中加载元素反应配置
 */
public class ElementReactionDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    
    // 存储所有加载的反应配置（按ID）
    private static final Map<String, ElementReactionConfig> REACTION_CONFIGS = new HashMap<>();
    
    // 存储元素对到反应的映射（保持与配置顺序一致，不排序，即有方向）
    private static final Map<String, ElementReactionConfig> ELEMENT_PAIR_REACTIONS = new HashMap<>();
    
    public ElementReactionDataLoader() {
        super(GSON, "element_reactions");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, 
                        ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        
        // 清空现有的配置
        REACTION_CONFIGS.clear();
        ELEMENT_PAIR_REACTIONS.clear();
        
        SpellElemental.LOGGER.info("开始加载元素反应配置...");
        
        int successCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            
            try {
                if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("variants")) {
                    // 新Schema：包含 variants
                    successCount += loadVariantsFile(resourceLocation, jsonElement.getAsJsonObject());
                } else {
                    // 旧Schema：不再支持
                    SpellElemental.LOGGER.warn("已忽略旧Schema元素反应配置(需升级为variants): {}", resourceLocation);
                    errorCount++;
                }
            } catch (JsonParseException e) {
                SpellElemental.LOGGER.error("解析元素反应配置失败: {} - {}", resourceLocation, e.getMessage());
                errorCount++;
            } catch (Exception e) {
                SpellElemental.LOGGER.error("加载元素反应配置时发生错误: {} - {}", resourceLocation, e.getMessage(), e);
                errorCount++;
            }
        }
        
        SpellElemental.LOGGER.info("元素反应配置加载完成: {} 个成功, {} 个错误", successCount, errorCount);
    }

    private int loadVariantsFile(ResourceLocation rl, JsonObject obj) {
        String reactionId = obj.has("reaction_id") ? obj.get("reaction_id").getAsString() : rl.getPath();
        String reactionName = obj.has("reaction_name") ? obj.get("reaction_name").getAsString() : reactionId;
        String description = obj.has("description") ? obj.get("description").getAsString() : "";
        int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 0;
        int count = 0;
        for (JsonElement variantEl : obj.getAsJsonArray("variants")) {
            JsonObject v = variantEl.getAsJsonObject();
            JsonObject ec = v.getAsJsonObject("element_conditions");
            JsonObject cons = v.getAsJsonObject("element_consumption");
            JsonObject dmg = v.getAsJsonObject("damage_effects");
            ElementReactionConfig cfg = new ElementReactionConfig();
            cfg.setReactionId(reactionId + ":" + count);
            cfg.setReactionName(reactionName);
            cfg.setDescription(description);
            cfg.setPriority(priority);
            cfg.setPrimaryElement(ec.get("primary_element").getAsString());
            cfg.setSecondaryElement(ec.get("secondary_element").getAsString());
            cfg.setPrimaryConsumption(cons.get("primary_consumption").getAsInt());
            cfg.setSecondaryConsumption(cons.get("secondary_consumption").getAsInt());
            cfg.setDamageMultiplier(dmg.has("damage_multiplier") ? dmg.get("damage_multiplier").getAsFloat() : 1.0f);
            ElementReactionConfig.ReactionEffects effects = new ElementReactionConfig.ReactionEffects();
            effects.setDamageType(dmg.has("reaction_type") ? dmg.get("reaction_type").getAsString() : "");
            if (dmg.has("area_damage")) {
                effects.setAreaDamage(dmg.get("area_damage").getAsBoolean());
            }
            if (dmg.has("area_radius")) {
                effects.setAreaRadius(dmg.get("area_radius").getAsFloat());
            }
            if (dmg.has("include_self")) {
                effects.setIncludeSelf(dmg.get("include_self").getAsBoolean());
            }
            if (dmg.has("damage_source")) {
                effects.setDamageSource(dmg.get("damage_source").getAsString());
            }
            cfg.setEffects(effects);
            if (validateConfig(cfg, rl)) {
                storeConfig(cfg);
                count++;
            } else {
                SpellElemental.LOGGER.warn("忽略无效反应变体: {} @ {}", cfg.getReactionId(), rl);
            }
        }
        return count;
    }
    
    /**
     * 验证配置的有效性
     */
    private boolean validateConfig(ElementReactionConfig config, ResourceLocation resourceLocation) {
        if (config == null) {
            SpellElemental.LOGGER.error("配置为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getReactionId() == null || config.getReactionId().trim().isEmpty()) {
            SpellElemental.LOGGER.error("反应ID不能为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getPrimaryElement() == null || config.getPrimaryElement().trim().isEmpty()) {
            SpellElemental.LOGGER.error("主导元素不能为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getSecondaryElement() == null || config.getSecondaryElement().trim().isEmpty()) {
            SpellElemental.LOGGER.error("被反应元素不能为空: {}", resourceLocation);
            return false;
        }
        
        if (config.getPrimaryConsumption() <= 0) {
            SpellElemental.LOGGER.error("主导元素消耗量必须大于0: {}", resourceLocation);
            return false;
        }
        
        if (config.getSecondaryConsumption() <= 0) {
            SpellElemental.LOGGER.error("被反应元素消耗量必须大于0: {}", resourceLocation);
            return false;
        }
        
        if (config.getEffects() == null) {
            SpellElemental.LOGGER.error("效果配置不能为空: {}", resourceLocation);
            return false;
        }
        
        return true;
    }
    
    private void storeConfig(ElementReactionConfig config) {
        REACTION_CONFIGS.put(config.getReactionId(), config);
        String elementPair = createOrderedPairKey(config.getPrimaryElement(), config.getSecondaryElement());
        ELEMENT_PAIR_REACTIONS.put(elementPair, config);
        SpellElemental.LOGGER.debug("加载元素反应配置: {} -> {} + {} (priority={})",
                config.getReactionName(), config.getPrimaryElement(), config.getSecondaryElement(), config.getPriority());
    }
    
    /**
     * 有序元素对键（不排序，保留方向）
     */
    public static String createOrderedPairKey(String element1, String element2) {
        return element1 + "+" + element2;
    }

    /**
     * （保留）无序元素对键（按字母序），供需要时使用
     */
    public static String createElementPairKey(String element1, String element2) {
        if (element1.compareTo(element2) <= 0) {
            return element1 + "+" + element2;
        } else {
            return element2 + "+" + element1;
        }
    }
    
    /**
     * 根据反应ID获取反应配置
     */
    public static ElementReactionConfig getReactionById(String reactionId) {
        return REACTION_CONFIGS.get(reactionId);
    }
    
    /**
     * 根据两个元素（有方向）获取可能的反应配置
     */
    public static ElementReactionConfig getReactionByElements(String element1, String element2) {
        String elementPair = createOrderedPairKey(element1, element2);
        return ELEMENT_PAIR_REACTIONS.get(elementPair);
    }
    
    /**
     * 检查两个元素之间是否存在反应（无序检查）
     */
    public static boolean hasReaction(String element1, String element2) {
        return getReactionByElements(element1, element2) != null
            || getReactionByElements(element2, element1) != null;
    }
    
    /**
     * 获取所有反应配置（按 priority 降序）
     */
    public static List<ElementReactionConfig> getAllReactionsSortedByPriority() {
        List<ElementReactionConfig> list = new ArrayList<>(REACTION_CONFIGS.values());
        list.sort(Comparator.comparingInt(ElementReactionConfig::getPriority).reversed());
        return list;
    }
    
    /**
     * 获取所有反应配置（拷贝）
     */
    public static Map<String, ElementReactionConfig> getAllReactions() {
        return new HashMap<>(REACTION_CONFIGS);
    }
    
    /**
     * 获取所有元素对反应映射（拷贝）
     */
    public static Map<String, ElementReactionConfig> getAllElementPairReactions() {
        return new HashMap<>(ELEMENT_PAIR_REACTIONS);
    }
} 