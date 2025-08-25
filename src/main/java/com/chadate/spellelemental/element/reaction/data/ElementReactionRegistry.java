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
    private static final Set<String> DAMAGE_REACTIONS = new LinkedHashSet<>();
    private static final Set<String> TICK_REACTIONS = new LinkedHashSet<>();
    /**
     * damage 类型组合索引：
     * key 形如 "a|b"（无序，a<=b 排序）或 "a->b"（有序，先手 a，后手 b）
     * value 为 reaction_id
     */
    private static final Map<String, String> DAMAGE_COMBO_INDEX = new HashMap<>();
    // 移除消耗量索引，新机制：前者消耗后手全部附着量，后手完全消失

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

    /**
     * 按方向的属性效果：key = "a->b"，value = attribute effects 列表
     */
    private static final Map<String, List<AttributeEffect>> DIRECTIONAL_ATTRIBUTE_EFFECTS = new HashMap<>();

    // -------------- tick 需求/消耗 --------------
    /** reaction_id -> TickRule 列表（支持变体） */
    private static final Map<String, List<TickRule>> TICK_RULES = new HashMap<>();

    // -------------- 反应是否消耗元素（damage触发） --------------
    /** reaction_id -> 是否消耗元素（默认 true） */
    private static final Map<String, Boolean> REACTION_CONSUME_FLAG = new HashMap<>();

    // -------------- 元素消耗比值（damage触发） --------------
    /** key = "source->target"，value = 消耗比值（source:target，默认1:1） */
    private static final Map<String, Double> DAMAGE_CONSUME_RATIO = new HashMap<>();

    private ElementReactionRegistry() {}

    public static void clear() {
        DAMAGE_REACTIONS.clear();
        TICK_REACTIONS.clear();
        DAMAGE_COMBO_INDEX.clear();
        REACTION_EFFECTS.clear();
        DIRECTIONAL_EFFECTS.clear();
        ATTRIBUTE_EFFECTS.clear();
        TICK_RULES.clear();
        REACTION_CONSUME_FLAG.clear();
        DAMAGE_CONSUME_RATIO.clear();
    }

    public static void add(String reactionId, String triggerType) {
        if (reactionId == null || reactionId.isBlank()) return;
        String t = triggerType == null ? "damage" : triggerType.trim().toLowerCase();
        if ("tick".equals(t)) {
            TICK_REACTIONS.add(reactionId);
        } else {
            DAMAGE_REACTIONS.add(reactionId);
        }
        // 默认：damage 类型的反应消耗元素；tick 类型不使用此标志
        if (!"tick".equals(t)) {
            REACTION_CONSUME_FLAG.put(reactionId, Boolean.TRUE);
        }
    }

    public static Set<String> getDamageReactions() {
        return Collections.unmodifiableSet(DAMAGE_REACTIONS);
    }

    public static Set<String> getTickReactions() {
        return Collections.unmodifiableSet(TICK_REACTIONS);
    }

    // -------------- consume flag API --------------
    public static void setConsumeFlag(String reactionId, boolean consume) {
        if (reactionId == null || reactionId.isBlank()) return;
        REACTION_CONSUME_FLAG.put(reactionId, consume);
    }

    /** 若未配置，默认返回 true（即消耗元素） */
    public static boolean shouldConsumeElements(String reactionId) {
        Boolean v = REACTION_CONSUME_FLAG.get(reactionId);
        return v == null || v;
    }

    // -------------- consume ratio API --------------
    /** 设置元素消耗比值 */
    public static void setConsumeRatio(String source, String target, double ratio) {
        if (source == null || source.isBlank() || target == null || target.isBlank()) return;
        String key = makeOrderedKey(safeElem(source), safeElem(target));
        DAMAGE_CONSUME_RATIO.put(key, Math.max(0.0, ratio));
    }

    /** 获取元素消耗比值，若未配置则返回1.0（即1:1） */
    public static double getConsumeRatio(String source, String target) {
        if (source == null || source.isBlank() || target == null || target.isBlank()) return 1.0;
        String key = makeOrderedKey(safeElem(source), safeElem(target));
        Double ratio = DAMAGE_CONSUME_RATIO.get(key);
        return ratio == null ? 1.0 : ratio;
    }

    /** 为 tick 反应追加一个规则（支持多个变体）。*/
    public static void setTickRule(String reactionId, Map<String, Integer> requirements, Map<String, Integer> consume, List<ReactionEffect> effects, int interval) {
        if (reactionId == null || reactionId.isBlank()) return;
        Map<String, Integer> req = normalizeElemIntMap(requirements);
        Map<String, Integer> con = normalizeElemIntMap(consume);
        List<ReactionEffect> effs = effects == null ? Collections.emptyList() : new ArrayList<>(effects);
        int iv = Math.max(1, interval);
        TICK_RULES.computeIfAbsent(reactionId, k -> new ArrayList<>()).add(new TickRule(req, con, effs, iv));
    }

    /** 获取某 tick 反应的所有规则（变体）。若无返回空列表。*/
    public static List<TickRule> getTickRules(String reactionId) {
        List<TickRule> list = TICK_RULES.get(reactionId);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
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
        } else {
            String key = makeOrderedKey(a, b);
            DAMAGE_COMBO_INDEX.put(key, reactionId);
        }
    }

    /**
     * 为有序组合建立索引（新机制：不再需要消耗量参数）。
     */
    public static void indexDamageOrdered(String reactionId, String source, String target) {
        if (reactionId == null || reactionId.isBlank()) return;
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return;
        String key = makeOrderedKey(a, b);
        DAMAGE_COMBO_INDEX.put(key, reactionId);
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

    // 移除 getDamageConsumeFor 方法，新机制不需要预定义消耗量

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
     * 为某个有序方向(source->target)添加属性效果。
     */
    public static void addDirectionalAttributeEffect(String source, String target, AttributeEffect effect) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty() || effect == null) return;
        String key = makeOrderedKey(a, b);
        DIRECTIONAL_ATTRIBUTE_EFFECTS.computeIfAbsent(key, k -> new ArrayList<>()).add(effect);
    }

    /**
     * 获取某个有序方向(source->target)的属性效果列表；若无返回空不可变列表。
     */
    public static List<AttributeEffect> getDirectionalAttributeEffects(String source, String target) {
        String a = safeElem(source);
        String b = safeElem(target);
        if (a.isEmpty() || b.isEmpty()) return Collections.emptyList();
        List<AttributeEffect> list = DIRECTIONAL_ATTRIBUTE_EFFECTS.get(makeOrderedKey(a, b));
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
        public final boolean damageAttacker; // 是否伤害攻击者（默认 false）
        public final boolean damageVictim;   // 是否伤害受害者（默认 true）
        
        // ---- 元素附着/消耗 扩展字段（可选）----
        // 支持 effects.attachment / effects.consume 这类元素操作
        // 当 type 为 "attachment" 或 "consume" 时才有意义
        public final String elementId;       // 目标元素ID（如 quicken）
        public final int elementAmount;      // 元素数量/强度
        public final float elementChance;    // 触发概率 [0,1]
        public final int elementDuration;    // 可选：持续时间（tick），0 表示未指定
        public final String elementOp;       // 操作类型："attachment" 或 "consume"

        public ReactionEffect(String type, float multiplier, String formula) {
            this.type = type;
            this.multiplier = multiplier;
            this.formula = formula;
            this.radius = 0f;
            this.damageType = null;
            this.damageAttacker = true;
            this.damageVictim = false;
            // 元素操作默认值
            this.elementId = null;
            this.elementAmount = 0;
            this.elementChance = 0f;
            this.elementDuration = 0;
            this.elementOp = null;
        }

        public ReactionEffect(String type, float multiplier, String formula, float radius, String damageType) {
            this.type = type;
            this.multiplier = multiplier;
            this.formula = formula == null ? "" : formula.trim();
            this.radius = radius;
            this.damageType = damageType == null ? "" : damageType.trim();
            this.damageAttacker = false;
            this.damageVictim = true;
            // 元素操作默认值
            this.elementId = null;
            this.elementAmount = 0;
            this.elementChance = 0f;
            this.elementDuration = 0;
            this.elementOp = null;
        }

        public ReactionEffect(String type, float multiplier, String formula, float radius, String damageType,
                               boolean damageAttacker, boolean damageVictim) {
            this.type = type == null ? "" : type.trim().toLowerCase();
            this.multiplier = multiplier;
            this.formula = formula == null ? "" : formula.trim();
            this.radius = radius;
            this.damageType = damageType == null ? "" : damageType.trim();
            this.damageAttacker = damageAttacker;
            this.damageVictim = damageVictim;
            // 元素操作默认值
            this.elementId = null;
            this.elementAmount = 0;
            this.elementChance = 0f;
            this.elementDuration = 0;
            this.elementOp = null;
        }

        /**
         * 元素操作效果构造器（数据驱动）
         * 当 type 为 "attachment" 或 "consume" 时，使用该构造函数。
         * @param type           效果类型（attachment / consume）
         * @param elementId      目标元素ID
         * @param elementAmount  数量/强度
         * @param elementChance  触发概率 [0,1]
         * @param elementDuration 可选持续时间（tick），无需可填 0
         */
        public ReactionEffect(String type, String elementId, int elementAmount, float elementChance, int elementDuration) {
            this.type = type == null ? "" : type.trim().toLowerCase();
            this.multiplier = 0f;
            this.formula = "";
            this.radius = 0f;
            this.damageType = "";
            this.damageAttacker = false;
            this.damageVictim = true;

            this.elementId = elementId == null ? null : elementId.trim().toLowerCase();
            this.elementAmount = elementAmount;
            this.elementChance = elementChance;
            this.elementDuration = elementDuration;
            this.elementOp = this.type; // 与 type 一致（attachment / consume）
        }
    }

    /**
         * 属性效果数据。
         * attributeId: 属性资源ID字符串
         * operation: 计算方式标识（如 add / multiply 等，由运行时解释）
         * value: 数值
         * duration: 持续时长（tick）
         */
        public record AttributeEffect(String attributeId, String operation, double value, int duration) {
            public AttributeEffect(String attributeId, String operation, double value, int duration) {
                this.attributeId = attributeId == null ? "" : attributeId.trim();
                this.operation = operation == null ? "add" : operation.trim().toLowerCase();
                this.value = value;
                this.duration = Math.max(0, duration);
            }
        }

    /**
     * tick 类型的规则：元素需求、消耗、效果与触发间隔
     *
     * @param requirements 触发所需的元素及其最小值
     * @param consume      触发后要消耗的元素量
     * @param effects      触发后可执行的效果（与 damage 类似的结构）
     * @param interval     触发间隔（单位：tick，最小为1；1=每tick）
     */
        public record TickRule(Map<String, Integer> requirements, Map<String, Integer> consume,
                               List<ReactionEffect> effects, int interval) {
            public TickRule(Map<String, Integer> requirements, Map<String, Integer> consume, List<ReactionEffect> effects, int interval) {
                this.requirements = requirements == null ? Collections.emptyMap() : Map.copyOf(requirements);
                this.consume = consume == null ? Collections.emptyMap() : Map.copyOf(consume);
                this.effects = effects == null ? Collections.emptyList() : List.copyOf(effects);
                this.interval = Math.max(1, interval);
            }
        }
}
