package com.chadate.spellelemental.element.reaction.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 反应注册表（内存态）
 * 维护从数据包加载到的反应ID集合，
 * 并按触发类型（damage / tick）分组。
 */
public final class ElementReactionRegistry {
    private static final Set<String> REACTION_IDS = new LinkedHashSet<>();
    private static final Set<String> DAMAGE_REACTIONS = new LinkedHashSet<>();
    private static final Set<String> TICK_REACTIONS = new LinkedHashSet<>();
    /**
     * damage 类型组合索引：
     * key 形如 "a|b"（无序，a<=b 排序）或 "a->b"（有序，先手 a，后手 b）
     * value 为 reaction_id
     */
    private static final Map<String, String> DAMAGE_COMBO_INDEX = new HashMap<>();
    /**
     * damage 类型组合的消耗量索引：
     * key 同上；value 为长度2的数组，index0=source消耗，index1=target消耗。
     */
    private static final Map<String, int[]> DAMAGE_CONSUME_INDEX = new HashMap<>();

    /**
     * 反应效果注册：reaction_id -> effects 列表
     */
    private static final Map<String, List<ReactionEffect>> REACTION_EFFECTS = new HashMap<>();

    /**
     * 按方向的反应效果：key = "a->b"，value = effects 列表
     */
    private static final Map<String, List<ReactionEffect>> DIRECTIONAL_EFFECTS = new HashMap<>();

    /**
     * 根级属性效果：reaction_id -> attribute effects 列表
     */
    private static final Map<String, List<AttributeEffect>> ATTRIBUTE_EFFECTS = new HashMap<>();

    // -------------- tick 需求/消耗 --------------
    /** reaction_id -> TickRule */
    private static final Map<String, TickRule> TICK_RULES = new HashMap<>();

    private ElementReactionRegistry() {}

    public static void clear() {
        REACTION_IDS.clear();
        DAMAGE_REACTIONS.clear();
        TICK_REACTIONS.clear();
        DAMAGE_COMBO_INDEX.clear();
        DAMAGE_CONSUME_INDEX.clear();
        REACTION_EFFECTS.clear();
        DIRECTIONAL_EFFECTS.clear();
        ATTRIBUTE_EFFECTS.clear();
        TICK_RULES.clear();
    }

    public static void add(String reactionId) {
        if (reactionId != null && !reactionId.isBlank()) {
            REACTION_IDS.add(reactionId);
        }
    }

    public static void add(String reactionId, String triggerType) {
        if (reactionId == null || reactionId.isBlank()) return;
        REACTION_IDS.add(reactionId);
        String t = triggerType == null ? "damage" : triggerType.trim().toLowerCase();
        if ("tick".equals(t)) {
            TICK_REACTIONS.add(reactionId);
        } else {
            DAMAGE_REACTIONS.add(reactionId);
        }
    }

    public static Set<String> getAll() {
        return Collections.unmodifiableSet(REACTION_IDS);
    }

    public static boolean contains(String reactionId) {
        return REACTION_IDS.contains(reactionId);
    }

    public static Set<String> getDamageReactions() {
        return Collections.unmodifiableSet(DAMAGE_REACTIONS);
    }

    public static Set<String> getTickReactions() {
        return Collections.unmodifiableSet(TICK_REACTIONS);
    }

    /** 为 tick 反应设置规则（覆盖式）。*/
    public static void setTickRule(String reactionId, Map<String, Integer> requirements, Map<String, Integer> consume, List<ReactionEffect> effects, int interval) {
        if (reactionId == null || reactionId.isBlank()) return;
        Map<String, Integer> req = normalizeElemIntMap(requirements);
        Map<String, Integer> con = normalizeElemIntMap(consume);
        List<ReactionEffect> effs = effects == null ? Collections.emptyList() : new ArrayList<>(effects);
        int iv = Math.max(1, interval);
        TICK_RULES.put(reactionId, new TickRule(req, con, effs, iv));
    }

    /** 获取某 tick 反应的规则，若无返回空规则。*/
    public static TickRule getTickRule(String reactionId) {
        TickRule r = TICK_RULES.get(reactionId);
        return r == null ? new TickRule(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), 1) : r;
    }

    private static Map<String, Integer> normalizeElemIntMap(Map<String, Integer> in) {
        Map<String, Integer> out = new HashMap<>();
        if (in != null) {
            for (Map.Entry<String, Integer> e : in.entrySet()) {
                String k = safeElem(e.getKey());
                if (!k.isEmpty()) out.put(k, Math.max(0, e.getValue() == null ? 0 : e.getValue()));
            }
        }
        return out;
    }

    // -------------- damage 组合索引 --------------

    /**
     * 为 damage 类型反应建立组合索引。
     * participants 建议为 2 个元素名；当 commutative=false 时，participants[0] 视为先手，participants[1] 为后手。
     */
    public static void indexDamageCombination(String reactionId, List<String> participants, boolean commutative) {
        if (reactionId == null || reactionId.isBlank() || participants == null || participants.size() < 2) return;
        String a = safeElem(participants.get(0));
        String b = safeElem(participants.get(1));
        if (a.isEmpty() || b.isEmpty()) return;
        if (commutative) {
            String key = makeUnorderedKey(a, b);
            DAMAGE_COMBO_INDEX.put(key, reactionId);
            DAMAGE_CONSUME_INDEX.putIfAbsent(key, new int[]{1, 1});
        } else {
            String key = makeOrderedKey(a, b);
            DAMAGE_COMBO_INDEX.put(key, reactionId);
            DAMAGE_CONSUME_INDEX.putIfAbsent(key, new int[]{1, 1});
        }
    }

    /**
     * 为有序组合设置自定义消耗量。
     */
    public static void indexDamageOrderedWithConsume(String reactionId, String source, String target, int consumeSource, int consumeTarget) {
        if (reactionId == null || reactionId.isBlank()) return;
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return;
        String key = makeOrderedKey(a, b);
        DAMAGE_COMBO_INDEX.put(key, reactionId);
        DAMAGE_CONSUME_INDEX.put(key, new int[]{Math.max(0, consumeSource), Math.max(0, consumeTarget)});
    }

    /** 查找 damage 组合（兼容无序/有序）。返回匹配到的 reaction_id 或 null。 */
    public static String findDamageReactionId(String source, String target) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return null;
        // 先查有序键
        String id = DAMAGE_COMBO_INDEX.get(makeOrderedKey(a, b));
        if (id != null) return id;
        // 再查无序键
        return DAMAGE_COMBO_INDEX.get(makeUnorderedKey(a, b));
    }

    /** 获取指定方向的消耗量，返回长度2数组，默认 {1,1}。 */
    public static int[] getDamageConsumeFor(String source, String target) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return new int[]{1,1};
        int[] v = DAMAGE_CONSUME_INDEX.get(makeOrderedKey(a, b));
        if (v != null) return new int[]{v[0], v[1]};
        // 对无序组合，消耗视为对称，两侧同值（取存储或默认1）
        v = DAMAGE_CONSUME_INDEX.get(makeUnorderedKey(a, b));
        if (v != null) return new int[]{v[0], v[1]};
        return new int[]{1,1};
    }

    public static boolean hasAnyDamageCombos() {
        return !DAMAGE_COMBO_INDEX.isEmpty();
    }

    private static String makeUnorderedKey(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        return x + "|" + y;
    }

    private static String makeOrderedKey(String a, String b) {
        return a + "->" + b;
    }

    private static String safeElem(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    // -------------- 通用效果存取 --------------

    /**
     * 将效果添加到指定 reactionId。保持数据驱动，不做业务约束。
     */
    public static void addEffect(String reactionId, ReactionEffect effect) {
        if (reactionId == null || reactionId.isBlank() || effect == null) return;
        REACTION_EFFECTS.computeIfAbsent(reactionId, k -> new ArrayList<>()).add(effect);
    }

    /**
     * 获取某反应的效果列表，若无则返回空不可变列表。
     */
    public static List<ReactionEffect> getEffects(String reactionId) {
        List<ReactionEffect> list = REACTION_EFFECTS.get(reactionId);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * 添加根级属性效果。
     */
    public static void addAttributeEffect(String reactionId, AttributeEffect effect) {
        if (reactionId == null || reactionId.isBlank() || effect == null) return;
        ATTRIBUTE_EFFECTS.computeIfAbsent(reactionId, k -> new ArrayList<>()).add(effect);
    }

    /**
     * 获取根级属性效果列表，若无返回空不可变列表。
     */
    public static List<AttributeEffect> getAttributeEffects(String reactionId) {
        List<AttributeEffect> list = ATTRIBUTE_EFFECTS.get(reactionId);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * 为某个有序方向(source->target)添加效果。
     */
    public static void addDirectionalEffect(String source, String target, ReactionEffect effect) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty() || effect == null) return;
        String key = makeOrderedKey(a, b);
        DIRECTIONAL_EFFECTS.computeIfAbsent(key, k -> new ArrayList<>()).add(effect);
    }

    /**
     * 获取某个有序方向(source->target)的效果列表；若无返回空不可变列表。
     */
    public static List<ReactionEffect> getDirectionalEffects(String source, String target) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return Collections.emptyList();
        List<ReactionEffect> list = DIRECTIONAL_EFFECTS.get(makeOrderedKey(a, b));
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * 通用的反应效果数据结构。
     * 基础通用字段：type、multiplier、formula。
     * 扩展可选字段（用于特定类型，如 aoe）：radius、damageType。
     * 具体解释由运行时处理器按 type 决定。
     */
    public static class ReactionEffect {
        public final String type;            // "aoe" 等
        public final float multiplier;       // 伤害倍率
        public final String formula;         // 可选公式名
        public final float radius;           // 仅 aoe 使用
        public final String damageType;      // 可选伤害类型 RL
        public final boolean attachElement;  // AOE 造成的伤害是否会引发元素附着/反应（默认 true）
        public final boolean damageAttacker; // 是否对攻击者结算伤害（默认 true，保持当前行为）
        public final boolean damageVictim;   // 是否对受击者结算伤害（默认 false，保持当前行为）

        public ReactionEffect(String type, float multiplier, String formula) {
            this.type = type == null ? "" : type.trim().toLowerCase();
            this.multiplier = multiplier;
            this.formula = formula == null ? "" : formula.trim();
            this.radius = 0f;
            this.damageType = "";
            this.attachElement = true;
            this.damageAttacker = true;
            this.damageVictim = false;
        }

        public ReactionEffect(String type, float multiplier, String formula, float radius, String damageType, boolean attachElement) {
            this.type = type == null ? "" : type.trim().toLowerCase();
            this.multiplier = multiplier;
            this.formula = formula == null ? "" : formula.trim();
            this.radius = radius;
            this.damageType = damageType == null ? "" : damageType.trim();
            this.attachElement = attachElement;
            this.damageAttacker = true;
            this.damageVictim = false;
        }

        public ReactionEffect(String type, float multiplier, String formula, float radius, String damageType, boolean attachElement,
                               boolean damageAttacker, boolean damageVictim) {
            this.type = type == null ? "" : type.trim().toLowerCase();
            this.multiplier = multiplier;
            this.formula = formula == null ? "" : formula.trim();
            this.radius = radius;
            this.damageType = damageType == null ? "" : damageType.trim();
            this.attachElement = attachElement;
            this.damageAttacker = damageAttacker;
            this.damageVictim = damageVictim;
        }
    }

    /**
     * 属性效果数据。
     * attributeId: 属性资源ID字符串
     * operation: 计算方式标识（如 add / multiply 等，由运行时解释）
     * value: 数值
     * duration: 持续时长（tick）
     */
    public static class AttributeEffect {
        public final String attributeId;
        public final String operation;
        public final double value;
        public final int duration;

        public AttributeEffect(String attributeId, String operation, double value, int duration) {
            this.attributeId = attributeId == null ? "" : attributeId.trim();
            this.operation = operation == null ? "add" : operation.trim().toLowerCase();
            this.value = value;
            this.duration = Math.max(0, duration);
        }
    }

    /** tick 类型的规则：元素需求、消耗、效果与触发间隔 */
    public static class TickRule {
        public final Map<String, Integer> requirements; // 触发所需的元素及其最小值
        public final Map<String, Integer> consume;       // 触发后要消耗的元素量
        public final List<ReactionEffect> effects;       // 触发后可执行的效果（与 damage 类似的结构）
        public final int interval;                       // 触发间隔（单位：tick，最小为1；1=每tick）

        public TickRule(Map<String, Integer> requirements, Map<String, Integer> consume, List<ReactionEffect> effects, int interval) {
            this.requirements = requirements == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(requirements));
            this.consume = consume == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(consume));
            this.effects = effects == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(effects));
            this.interval = Math.max(1, interval);
        }
    }
}
