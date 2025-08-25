package com.chadate.spellelemental.element.reaction.data;

import com.chadate.spellelemental.SpellElemental;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.Map;

/**
 * 元素反应数据加载器（最小结构）
 * 数据位置：data/<namespace>/element_reactions/*.json
 * 新字段：
 *  - elements: ["ice","fire"] 无序组合
 *  - ordered: [["fire","ice"],["ice","fire"]] 有序组合（先后手）
 */
public class ElementReactionDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ElementReactionDataLoader() {
        super(GSON, "element_reactions");
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object,
                         @Nonnull ResourceManager resourceManager,
                         @Nonnull ProfilerFiller profiler) {
        ElementReactionRegistry.clear();
        int ok = 0;
        int err = 0;
        SpellElemental.LOGGER.info("开始加载元素反应 JSON，共 {} 项", object.size());
        for (Map.Entry<ResourceLocation, JsonElement> e : object.entrySet()) {
            ResourceLocation id = e.getKey();
            try {
                JsonObject root = e.getValue().getAsJsonObject();
                // 仅提取必要字段，避免 ordered 的异构类型导致整体反序列化失败
                String reactionId = root.has("reaction_id") && root.get("reaction_id").isJsonPrimitive()
                        ? root.get("reaction_id").getAsString() : null;
                String triggerType = root.has("trigger_type") && root.get("trigger_type").isJsonPrimitive()
                        ? root.get("trigger_type").getAsString() : null;
                String trigger = triggerType == null ? "damage" : triggerType.trim().toLowerCase();
                // 读取是否消耗元素（仅 damage 类型有意义），默认 true
                boolean consumeElements = true;
                if (root.has("consume_elements") && root.get("consume_elements").isJsonPrimitive()) {
                    try { consumeElements = root.get("consume_elements").getAsBoolean(); } catch (Exception ignore) {}
                }

                if (reactionId == null || reactionId.isBlank()) {
                    SpellElemental.LOGGER.error("元素反应配置缺少 reaction_id: {}", id);
                    err++;
                    continue;
                } else if ("tick".equals(trigger)) {
                    // 禁止 tick 类型使用方向化配置（如 ordered/source/target）。一旦发现则跳过该条配置。
                    if (root.has("ordered")) {
                        SpellElemental.LOGGER.error("[元素反应: {}] tick 类型不支持方向化配置(ordered)", reactionId);
                        err++;
                        continue;
                    }
                    // 新增：支持变体写法 variants: [ { requirements, consume, interval, effects }, ... ]
                    if (root.has("variants") && root.get("variants").isJsonArray()) {
                        JsonArray vars = root.getAsJsonArray("variants");
                        for (JsonElement varEl : vars) {
                            if (!varEl.isJsonObject()) continue;
                            JsonObject var = varEl.getAsJsonObject();
                            // 解析单变体 requirements/consume
                            java.util.Map<String, Integer> reqMap = new java.util.HashMap<>();
                            if (var.has("requirements") && var.get("requirements").isJsonObject()) {
                                JsonObject req = var.getAsJsonObject("requirements");
                                for (Map.Entry<String, JsonElement> en : req.entrySet()) {
                                    if (en.getValue().isJsonPrimitive()) {
                                        reqMap.put(en.getKey(), Math.max(0, en.getValue().getAsInt()));
                                    }
                                }
                            }
                            java.util.Map<String, Integer> conMap = new java.util.HashMap<>();
                            if (var.has("consume") && var.get("consume").isJsonObject()) {
                                JsonObject con = var.getAsJsonObject("consume");
                                for (Map.Entry<String, JsonElement> en : con.entrySet()) {
                                    if (en.getValue().isJsonPrimitive()) {
                                        conMap.put(en.getKey(), Math.max(0, en.getValue().getAsInt()));
                                    }
                                }
                            }
                            // 解析 interval
                            int interval = 1;
                            if (var.has("interval") && var.get("interval").isJsonPrimitive()) {
                                try { interval = Math.max(1, var.get("interval").getAsInt()); } catch (Exception ignore) { interval = 1; }
                            } else if (root.has("interval") && root.get("interval").isJsonPrimitive()) {
                                // 兼容：变体未指定则回退根级 interval
                                try { interval = Math.max(1, root.get("interval").getAsInt()); } catch (Exception ignore) { interval = 1; }
                            }
                            // 解析 effects（兼容数组或 {"damage": [...]} 分组）
                            java.util.List<ElementReactionRegistry.ReactionEffect> tickEffects = new java.util.ArrayList<>();
                            if (var.has("effects")) {
                                if (var.get("effects").isJsonArray()) {
                                    JsonArray effects = var.getAsJsonArray("effects");
                                    for (JsonElement effEl : effects) {
                                        if (!effEl.isJsonObject()) continue;
                                        JsonObject eff = effEl.getAsJsonObject();
                                        String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                        // 支持数组写法中直接使用 type: attachment/consume
                                        if ("attachment".equalsIgnoreCase(type) || "consume".equalsIgnoreCase(type)) {
                                            String elemId = eff.has("element") && eff.get("element").isJsonPrimitive() ? eff.get("element").getAsString()
                                                    : (eff.has("element_id") && eff.get("element_id").isJsonPrimitive() ? eff.get("element_id").getAsString() : null);
                                            int amount = eff.has("amount") && eff.get("amount").isJsonPrimitive() ? eff.get("amount").getAsInt()
                                                    : (eff.has("value") && eff.get("value").isJsonPrimitive() ? eff.get("value").getAsInt() : 0);
                                            float chance = eff.has("chance") && eff.get("chance").isJsonPrimitive() ? eff.get("chance").getAsFloat() : 1.0f;
                                            int duration = eff.has("duration") && eff.get("duration").isJsonPrimitive() ? eff.get("duration").getAsInt() : 0;
                                            tickEffects.add(new ElementReactionRegistry.ReactionEffect(type, elemId, amount, chance, duration));
                                        } else {
                                            float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                            String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                            float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                            String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                            boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                            boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                            tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                                        }
                                    }
                                } else if (var.get("effects").isJsonObject()) {
                                    JsonObject grouped = var.getAsJsonObject("effects");
                                    if (grouped.has("damage") && grouped.get("damage").isJsonArray()) {
                                        JsonArray effects = grouped.getAsJsonArray("damage");
                                        for (JsonElement effEl : effects) {
                                            if (!effEl.isJsonObject()) continue;
                                            JsonObject eff = effEl.getAsJsonObject();
                                            String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                            float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                            String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                            float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                            String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                            boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                            boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                            tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                                        }
                                    }
                                    // 新增：分组写法 effects.attachment
                                    if (grouped.has("attachment") && grouped.get("attachment").isJsonArray()) {
                                        JsonArray attArr = grouped.getAsJsonArray("attachment");
                                        for (JsonElement attEl : attArr) {
                                            if (!attEl.isJsonObject()) continue;
                                            JsonObject att = attEl.getAsJsonObject();
                                            String elemId = att.has("element") && att.get("element").isJsonPrimitive() ? att.get("element").getAsString()
                                                    : (att.has("element_id") && att.get("element_id").isJsonPrimitive() ? att.get("element_id").getAsString() : null);
                                            int amount = att.has("amount") && att.get("amount").isJsonPrimitive() ? att.get("amount").getAsInt()
                                                    : (att.has("value") && att.get("value").isJsonPrimitive() ? att.get("value").getAsInt() : 0);
                                            float chance = att.has("chance") && att.get("chance").isJsonPrimitive() ? att.get("chance").getAsFloat() : 1.0f;
                                            int duration = att.has("duration") && att.get("duration").isJsonPrimitive() ? att.get("duration").getAsInt() : 0;
                                            tickEffects.add(new ElementReactionRegistry.ReactionEffect("attachment", elemId, amount, chance, duration));
                                        }
                                    }
                                    // 新增：分组写法 effects.potion
                                    if (grouped.has("potion") && grouped.get("potion").isJsonArray()) {
                                        JsonArray potionArr = grouped.getAsJsonArray("potion");
                                        for (JsonElement potionEl : potionArr) {
                                            if (!potionEl.isJsonObject()) continue;
                                            JsonObject potion = potionEl.getAsJsonObject();
                                            String potionId = potion.has("potion_id") && potion.get("potion_id").isJsonPrimitive() ? potion.get("potion_id").getAsString() : null;
                                            int potionDuration = potion.has("duration") && potion.get("duration").isJsonPrimitive() ? potion.get("duration").getAsInt() : 200;
                                            int potionLevel = potion.has("level") && potion.get("level").isJsonPrimitive() ? potion.get("level").getAsInt() - 1 : 0; // 转换为0-based
                                            float potionChance = potion.has("chance") && potion.get("chance").isJsonPrimitive() ? potion.get("chance").getAsFloat() : 1.0f;
                                            if (potionId != null) {
                                                tickEffects.add(new ElementReactionRegistry.ReactionEffect("potion", potionId, potionDuration, potionLevel, potionChance));
                                            }
                                        }
                                    }
                                }
                            } else if (root.has("effects")) {
                                // 兼容：变体没写 effects 则回退根级 effects
                                if (root.get("effects").isJsonArray()) {
                                    JsonArray effects = root.getAsJsonArray("effects");
                                    for (JsonElement effEl : effects) {
                                        if (!effEl.isJsonObject()) continue;
                                        JsonObject eff = effEl.getAsJsonObject();
                                        String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                        if ("attachment".equalsIgnoreCase(type) || "consume".equalsIgnoreCase(type)) {
                                            String elemId = eff.has("element") && eff.get("element").isJsonPrimitive() ? eff.get("element").getAsString()
                                                    : (eff.has("element_id") && eff.get("element_id").isJsonPrimitive() ? eff.get("element_id").getAsString() : null);
                                            int amount = eff.has("amount") && eff.get("amount").isJsonPrimitive() ? eff.get("amount").getAsInt()
                                                    : (eff.has("value") && eff.get("value").isJsonPrimitive() ? eff.get("value").getAsInt() : 0);
                                            float chance = eff.has("chance") && eff.get("chance").isJsonPrimitive() ? eff.get("chance").getAsFloat() : 1.0f;
                                            int duration = eff.has("duration") && eff.get("duration").isJsonPrimitive() ? eff.get("duration").getAsInt() : 0;
                                            tickEffects.add(new ElementReactionRegistry.ReactionEffect(type, elemId, amount, chance, duration));
                                        } else if ("potion".equalsIgnoreCase(type)) {
                                            String potionId = eff.has("potion_id") && eff.get("potion_id").isJsonPrimitive() ? eff.get("potion_id").getAsString() : null;
                                            int potionDuration = eff.has("duration") && eff.get("duration").isJsonPrimitive() ? eff.get("duration").getAsInt() : 200;
                                            int potionLevel = eff.has("level") && eff.get("level").isJsonPrimitive() ? eff.get("level").getAsInt() - 1 : 0; // 转换为0-based
                                            float potionChance = eff.has("chance") && eff.get("chance").isJsonPrimitive() ? eff.get("chance").getAsFloat() : 1.0f;
                                            if (potionId != null) {
                                                tickEffects.add(new ElementReactionRegistry.ReactionEffect("potion", potionId, potionDuration, potionLevel, potionChance));
                                            }
                                        } else {
                                            float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                            String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                            float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                            String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                            boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                            boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                            tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                                        }
                                    }
                                } else if (root.get("effects").isJsonObject()) {
                                    JsonObject grouped = root.getAsJsonObject("effects");
                                    if (grouped.has("damage") && grouped.get("damage").isJsonArray()) {
                                        JsonArray effects = grouped.getAsJsonArray("damage");
                                        for (JsonElement effEl : effects) {
                                            if (!effEl.isJsonObject()) continue;
                                            JsonObject eff = effEl.getAsJsonObject();
                                            String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                            float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                            String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                            float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                            String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                            boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                            boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                            tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                                        }
                                    }
                                    // 新增：根级 effects.attachment 回退
                                    if (grouped.has("attachment") && grouped.get("attachment").isJsonArray()) {
                                        JsonArray attArr = grouped.getAsJsonArray("attachment");
                                        for (JsonElement attEl : attArr) {
                                            if (!attEl.isJsonObject()) continue;
                                            JsonObject att = attEl.getAsJsonObject();
                                            String elemId = att.has("element") && att.get("element").isJsonPrimitive() ? att.get("element").getAsString()
                                                    : (att.has("element_id") && att.get("element_id").isJsonPrimitive() ? att.get("element_id").getAsString() : null);
                                            int amount = att.has("amount") && att.get("amount").isJsonPrimitive() ? att.get("amount").getAsInt()
                                                    : (att.has("value") && att.get("value").isJsonPrimitive() ? att.get("value").getAsInt() : 0);
                                            float chance = att.has("chance") && att.get("chance").isJsonPrimitive() ? att.get("chance").getAsFloat() : 1.0f;
                                            int duration = att.has("duration") && att.get("duration").isJsonPrimitive() ? att.get("duration").getAsInt() : 0;
                                            tickEffects.add(new ElementReactionRegistry.ReactionEffect("attachment", elemId, amount, chance, duration));
                                        }
                                    }
                                }
                            }
                            ElementReactionRegistry.setTickRule(reactionId.trim(), reqMap, conMap, tickEffects, interval);
                        }
                        ElementReactionRegistry.add(reactionId.trim(), "tick");
                        ok++;
                        continue;
                    }

                    // 旧写法：单一 requirements/consume/effects
                    java.util.Map<String, Integer> reqMap = new java.util.HashMap<>();
                    if (root.has("requirements") && root.get("requirements").isJsonObject()) {
                        JsonObject req = root.getAsJsonObject("requirements");
                        for (Map.Entry<String, JsonElement> en : req.entrySet()) {
                            if (en.getValue().isJsonPrimitive()) {
                                reqMap.put(en.getKey(), Math.max(0, en.getValue().getAsInt()));
                            }
                        }
                    }
                    java.util.Map<String, Integer> conMap = new java.util.HashMap<>();
                    if (root.has("consume") && root.get("consume").isJsonObject()) {
                        JsonObject con = root.getAsJsonObject("consume");
                        for (Map.Entry<String, JsonElement> en : con.entrySet()) {
                            if (en.getValue().isJsonPrimitive()) {
                                conMap.put(en.getKey(), Math.max(0, en.getValue().getAsInt()));
                            }
                        }
                    }
                    java.util.List<ElementReactionRegistry.ReactionEffect> tickEffects = new java.util.ArrayList<>();
                    int interval = 1;
                    if (root.has("interval") && root.get("interval").isJsonPrimitive()) {
                        try { interval = Math.max(1, root.get("interval").getAsInt()); } catch (Exception ignore) { interval = 1; }
                    }
                    if (root.has("effects")) {
                        if (root.get("effects").isJsonArray()) {
                            JsonArray effects = root.getAsJsonArray("effects");
                            for (JsonElement effEl : effects) {
                                if (!effEl.isJsonObject()) continue;
                                JsonObject eff = effEl.getAsJsonObject();
                                String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                        ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                        : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                            }
                        } else if (root.get("effects").isJsonObject()) {
                            JsonObject grouped = root.getAsJsonObject("effects");
                            if (grouped.has("damage") && grouped.get("damage").isJsonArray()) {
                                JsonArray effects = grouped.getAsJsonArray("damage");
                                for (JsonElement effEl : effects) {
                                    if (!effEl.isJsonObject()) continue;
                                    JsonObject eff = effEl.getAsJsonObject();
                                    String type = eff.has("type") && eff.get("type").isJsonPrimitive() ? eff.get("type").getAsString() : "";
                                    float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive() ? eff.get("multiplier").getAsFloat() : 1.0f;
                                    String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive() ? eff.get("formula").getAsString() : "";
                                    float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive() ? eff.get("radius").getAsFloat() : 0f;
                                    String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive() ? eff.get("damage_type").getAsString() : "";
                                    boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                    boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                    tickEffects.add((radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || damageVictim)
                                            ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                            : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula));
                                }
                            }
                        }
                    }
                    // 注册 tick 规则并记录反应
                    ElementReactionRegistry.setTickRule(reactionId.trim(), reqMap, conMap, tickEffects, interval);
                    ElementReactionRegistry.add(reactionId.trim(), "tick");
                    ok++;
                    // tick 类型完成，跳过后续 damage/effects 的通用解析
                    continue;
                }
                // 支持 trigger_type：缺省按 damage 归类
                ElementReactionRegistry.add(reactionId.trim(), trigger);
                // 若为 damage 类型，建立组合索引（仅支持新结构 ordered/elements）
                if ("damage".equals(trigger)) {
                    // 设置是否消耗元素标志
                    ElementReactionRegistry.setConsumeFlag(reactionId.trim(), consumeElements);
                    // 优先处理 ordered；允许两种形态：
                    //  1) 数组对：["a","b"]
                    //  2) 对象：{"source":"a","target":"b","consume":{"source":1,"target":2}}
                    if (root.has("ordered") && root.get("ordered").isJsonArray()) {
                        JsonArray arr = root.getAsJsonArray("ordered");
                        for (JsonElement el : arr) {
                            if (el.isJsonArray()) {
                                JsonArray pair = el.getAsJsonArray();
                                if (pair.size() >= 2 && pair.get(0).isJsonPrimitive() && pair.get(1).isJsonPrimitive()) {
                                    ElementReactionRegistry.indexDamageCombination(
                                            reactionId.trim(),
                                            List.of(pair.get(0).getAsString(), pair.get(1).getAsString()),
                                            false
                                    );
                                }
                            } else if (el.isJsonObject()) {
                                JsonObject obj = el.getAsJsonObject();
                                String src = obj.has("source") ? obj.get("source").getAsString() : null;
                                String tgt = obj.has("target") ? obj.get("target").getAsString() : null;
                                // 解析消耗比值（可选）
                                double ratio = 1.0;
                                if (obj.has("ratio") && obj.get("ratio").isJsonPrimitive()) {
                                    try {
                                        ratio = Math.max(0.0, obj.get("ratio").getAsDouble());
                                    } catch (Exception ex) {
                                        ratio = 1.0;
                                    }
                                }
                                if (src != null && tgt != null) {
                                    ElementReactionRegistry.indexDamageOrdered(
                                            reactionId.trim(), src, tgt
                                    );
                                    // 设置消耗比值
                                    if (ratio != 1.0) {
                                        ElementReactionRegistry.setConsumeRatio(src, tgt, ratio);
                                    }
                                    // 解析该方向下的 effects（可选）：兼容数组或分组对象({ "damage": [...] })
                                    if (obj.has("effects")) {
                                        if (obj.get("effects").isJsonArray()) {
                                            JsonArray dirEffs = obj.getAsJsonArray("effects");
                                            for (JsonElement de : dirEffs) {
                                                if (!de.isJsonObject()) continue;
                                                JsonObject eff = de.getAsJsonObject();
                                                String type = eff.has("type") && eff.get("type").isJsonPrimitive()
                                                        ? eff.get("type").getAsString() : "";
                                                float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive()
                                                        ? eff.get("multiplier").getAsFloat() : 1.0f;
                                                String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive()
                                                        ? eff.get("formula").getAsString() : "";
                                                float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive()
                                                        ? eff.get("radius").getAsFloat() : 0f;
                                                String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive()
                                                        ? eff.get("damage_type").getAsString() : "";
                                                boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                                boolean damageVictim = !eff.has("damage_victim") || (eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean());
                                                ElementReactionRegistry.addDirectionalEffect(
                                                        src, tgt,
                                                        (radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || !damageVictim)
                                                                ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                                : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                                                );
                                            }
                                        } else if (obj.get("effects").isJsonObject()) {
                                            JsonObject grouped = obj.getAsJsonObject("effects");
                                            if (grouped.has("damage") && grouped.get("damage").isJsonArray()) {
                                                JsonArray dirEffs = grouped.getAsJsonArray("damage");
                                                for (JsonElement de : dirEffs) {
                                                    if (!de.isJsonObject()) continue;
                                                    JsonObject eff = de.getAsJsonObject();
                                                    String type = eff.has("type") && eff.get("type").isJsonPrimitive()
                                                            ? eff.get("type").getAsString() : "";
                                                    float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive()
                                                            ? eff.get("multiplier").getAsFloat() : 1.0f;
                                                    String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive()
                                                            ? eff.get("formula").getAsString() : "";
                                                    float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive()
                                                            ? eff.get("radius").getAsFloat() : 0f;
                                                    String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive()
                                                            ? eff.get("damage_type").getAsString() : "";
                                                    boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                                    boolean damageVictim = !eff.has("damage_victim") || (eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean());
                                                    ElementReactionRegistry.addDirectionalEffect(
                                                            src, tgt,
                                                            (radius > 0f || !damageTypeStr.isEmpty() || !damageAttacker || !damageVictim)
                                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, damageAttacker, damageVictim)
                                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                                                    );
                                                }
                                            }
                                            // 解析有序方向下的属性效果组
                                            if (grouped.has("attribute") && grouped.get("attribute").isJsonArray()) {
                                                JsonArray attrEffs = grouped.getAsJsonArray("attribute");
                                                for (JsonElement ae : attrEffs) {
                                                    if (!ae.isJsonObject()) continue;
                                                    JsonObject aobj = ae.getAsJsonObject();
                                                    String attrId = aobj.has("attribute_id") && aobj.get("attribute_id").isJsonPrimitive() ? aobj.get("attribute_id").getAsString() : "";
                                                    String op = aobj.has("type") && aobj.get("type").isJsonPrimitive() ? aobj.get("type").getAsString() : "add";
                                                    double val = aobj.has("value") && aobj.get("value").isJsonPrimitive() ? aobj.get("value").getAsDouble() : 0.0;
                                                    int dur = aobj.has("duration") && aobj.get("duration").isJsonPrimitive() ? Math.max(0, aobj.get("duration").getAsInt()) : 0;
                                                    // 使用方向化属性效果注册，而不是全局注册
                                                    ElementReactionRegistry.addDirectionalAttributeEffect(src, tgt,
                                                            new ElementReactionRegistry.AttributeEffect(attrId, op, val, dur));
                                                }
                                            }
                                            // 解析有序方向下的元素附着效果组（attachment）
                                            if (grouped.has("attachment") && grouped.get("attachment").isJsonArray()) {
                                                JsonArray attEffs = grouped.getAsJsonArray("attachment");
                                                for (JsonElement ae : attEffs) {
                                                    if (!ae.isJsonObject()) continue;
                                                    JsonObject aobj = ae.getAsJsonObject();
                                                    String elementId = aobj.has("element_id") && aobj.get("element_id").isJsonPrimitive()
                                                            ? aobj.get("element_id").getAsString() : null;
                                                    int amount = aobj.has("amount") && aobj.get("amount").isJsonPrimitive()
                                                            ? Math.max(0, aobj.get("amount").getAsInt()) : 0;
                                                    float chance = aobj.has("chance") && aobj.get("chance").isJsonPrimitive()
                                                            ? aobj.get("chance").getAsFloat() : 1.0f;
                                                    int duration = aobj.has("duration") && aobj.get("duration").isJsonPrimitive()
                                                            ? Math.max(0, aobj.get("duration").getAsInt()) : 0;
                                                    if (elementId != null && !elementId.isBlank() && amount > 0) {
                                                        ElementReactionRegistry.addDirectionalEffect(
                                                                src, tgt,
                                                                new ElementReactionRegistry.ReactionEffect("attachment", elementId, amount, chance, duration)
                                                        );
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (root.has("elements") && root.get("elements").isJsonArray()) {
                        JsonArray el = root.getAsJsonArray("elements");
                        if (el.size() >= 2 && el.get(0).isJsonPrimitive() && el.get(1).isJsonPrimitive()) {
                            ElementReactionRegistry.indexDamageCombination(
                                    reactionId.trim(),
                                    List.of(el.get(0).getAsString(), el.get(1).getAsString()),
                                    true
                            );
                        } else {
                            SpellElemental.LOGGER.warn("元素反应缺少有效的 elements 字段: {}", id);
                        }
                    } else {
                        SpellElemental.LOGGER.warn("元素反应缺少有效的 ordered/elements 组合字段: {}", id);
                    }
                }

                // 解析 effects（仅非 tick）：支持数组或分组对象({ "damage": [...] })
                if (!"tick".equals(trigger) && root.has("effects")) {
                    if (root.get("effects").isJsonArray()) {
                        JsonArray effects = root.getAsJsonArray("effects");
                        for (JsonElement effEl : effects) {
                            if (!effEl.isJsonObject()) continue;
                            JsonObject eff = effEl.getAsJsonObject();
                            String type = eff.has("type") && eff.get("type").isJsonPrimitive()
                                    ? eff.get("type").getAsString() : "";
                            float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive()
                                    ? eff.get("multiplier").getAsFloat() : 1.0f;
                            String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive()
                                    ? eff.get("formula").getAsString() : "";
                            float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive()
                                    ? eff.get("radius").getAsFloat() : 0f;
                            String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive()
                                    ? eff.get("damage_type").getAsString() : "";
                            ElementReactionRegistry.addEffect(
                                    reactionId.trim(),
                                    (radius > 0f || !damageTypeStr.isEmpty())
                                            ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr)
                                            : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                            );
                        }
                    } else if (root.get("effects").isJsonObject()) {
                        JsonObject grouped = root.getAsJsonObject("effects");
                        if (grouped.has("damage") && grouped.get("damage").isJsonArray()) {
                            JsonArray effects = grouped.getAsJsonArray("damage");
                            for (JsonElement effEl : effects) {
                                if (!effEl.isJsonObject()) continue;
                                JsonObject eff = effEl.getAsJsonObject();
                                String type = eff.has("type") && eff.get("type").isJsonPrimitive()
                                        ? eff.get("type").getAsString() : "";
                                float multiplier = eff.has("multiplier") && eff.get("multiplier").isJsonPrimitive()
                                        ? eff.get("multiplier").getAsFloat() : 1.0f;
                                String formula = eff.has("formula") && eff.get("formula").isJsonPrimitive()
                                        ? eff.get("formula").getAsString() : "";
                                float radius = eff.has("radius") && eff.get("radius").isJsonPrimitive()
                                        ? eff.get("radius").getAsFloat() : 0f;
                                String damageTypeStr = eff.has("damage_type") && eff.get("damage_type").isJsonPrimitive()
                                        ? eff.get("damage_type").getAsString() : "";
                                ElementReactionRegistry.addEffect(
                                        reactionId.trim(),
                                        (radius > 0f || !damageTypeStr.isEmpty())
                                                ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr)
                                                : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                                );
                            }
                        }
                        // 解析根级别的属性效果组
                        if (grouped.has("attribute") && grouped.get("attribute").isJsonArray()) {
                            JsonArray attrEffs = grouped.getAsJsonArray("attribute");
                            for (JsonElement ae : attrEffs) {
                                if (!ae.isJsonObject()) continue;
                                JsonObject aobj = ae.getAsJsonObject();
                                String attrId = aobj.has("attribute_id") && aobj.get("attribute_id").isJsonPrimitive() ? aobj.get("attribute_id").getAsString() : "";
                                String op = aobj.has("type") && aobj.get("type").isJsonPrimitive() ? aobj.get("type").getAsString() : "add";
                                double val = aobj.has("value") && aobj.get("value").isJsonPrimitive() ? aobj.get("value").getAsDouble() : 0.0;
                                int dur = aobj.has("duration") && aobj.get("duration").isJsonPrimitive() ? Math.max(0, aobj.get("duration").getAsInt()) : 0;
                                ElementReactionRegistry.addAttributeEffect(reactionId.trim(),
                                        new ElementReactionRegistry.AttributeEffect(attrId, op, val, dur));
                            }
                        }
                    }
                }
                ok++;
            } catch (JsonParseException ex) {
                SpellElemental.LOGGER.error("解析元素反应失败 {}: {}", id, ex.getMessage());
                err++;
            } catch (Exception ex) {
                SpellElemental.LOGGER.error("加载元素反应出错 {}: {}", id, ex.getMessage(), ex);
                err++;
            }
        }
        SpellElemental.LOGGER.info("元素反应加载完成: {} 个成功, {} 个错误", ok, err);
    }
}
