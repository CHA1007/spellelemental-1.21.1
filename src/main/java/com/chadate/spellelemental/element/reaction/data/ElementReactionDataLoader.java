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
                    // 解析 tick 类型：requirements、consume 以及可选 effects
                    // requirements: { "fire": 100, "ice": 50 }
                    // consume: { "fire": 20, "ice": 10 }
                    // effects: 支持与 damage 相同的结构（数组或分组对象 {"damage":[...]}）
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
                        try {
                            interval = Math.max(1, root.get("interval").getAsInt());
                        } catch (Exception ignore) { interval = 1; }
                    }
                    if (root.has("effects")) {
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
                                boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                                // tick AOE 默认不伤害中心实体（与旧行为一致）；仅当显式为 true 时才伤害中心
                                boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                tickEffects.add(
                                        (radius > 0f || !damageTypeStr.isEmpty() || !attach || !damageAttacker || damageVictim)
                                                ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach, damageAttacker, damageVictim)
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
                                    boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                                    // tick AOE 默认不伤害中心实体（与旧行为一致）；仅当显式为 true 时才伤害中心
                                    boolean damageAttacker = eff.has("damage_attacker") && eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean();
                                    boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                    tickEffects.add(
                                            (radius > 0f || !damageTypeStr.isEmpty() || !attach || !damageAttacker || damageVictim)
                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach, damageAttacker, damageVictim)
                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                                    );
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
                                int cs = 1, ct = 1;
                                if (obj.has("consume") && obj.get("consume").isJsonObject()) {
                                    JsonObject c = obj.getAsJsonObject("consume");
                                    if (c.has("source")) cs = Math.max(0, c.get("source").getAsInt());
                                    if (c.has("target")) ct = Math.max(0, c.get("target").getAsInt());
                                }
                                if (src != null && tgt != null) {
                                    ElementReactionRegistry.indexDamageOrderedWithConsume(
                                            reactionId.trim(), src, tgt, cs, ct
                                    );
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
                                                boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                                                boolean damageAttacker = !eff.has("damage_attacker") || (eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean());
                                                boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                                ElementReactionRegistry.addDirectionalEffect(
                                                        src, tgt,
                                                        (radius > 0f || !damageTypeStr.isEmpty() || !attach || !damageAttacker || damageVictim)
                                                                ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach, damageAttacker, damageVictim)
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
                                                    boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                                                    boolean damageAttacker = !eff.has("damage_attacker") || (eff.get("damage_attacker").isJsonPrimitive() && eff.get("damage_attacker").getAsBoolean());
                                                    boolean damageVictim = eff.has("damage_victim") && eff.get("damage_victim").isJsonPrimitive() && eff.get("damage_victim").getAsBoolean();
                                                    ElementReactionRegistry.addDirectionalEffect(
                                                            src, tgt,
                                                            (radius > 0f || !damageTypeStr.isEmpty() || !attach || !damageAttacker || damageVictim)
                                                                    ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach, damageAttacker, damageVictim)
                                                                    : new ElementReactionRegistry.ReactionEffect(type, multiplier, formula)
                                                    );
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
                            boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                            ElementReactionRegistry.addEffect(
                                    reactionId.trim(),
                                    (radius > 0f || !damageTypeStr.isEmpty() || !attach)
                                            ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach)
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
                                boolean attach = !eff.has("attach_element") || (eff.get("attach_element").isJsonPrimitive() && eff.get("attach_element").getAsBoolean());
                                ElementReactionRegistry.addEffect(
                                        reactionId.trim(),
                                        (radius > 0f || !damageTypeStr.isEmpty() || !attach)
                                                ? new ElementReactionRegistry.ReactionEffect(type, multiplier, formula, radius, damageTypeStr, attach)
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
