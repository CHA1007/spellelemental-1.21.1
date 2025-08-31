package com.chadate.spellelemental.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.*;

public class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 应用于任何法术的默认元素附加量，但没有显式覆盖
    public static final IntValue ELEMENT_ATTACHMENT_DEFAULT;

    // 元件附件 ICD 设置
    public static final IntValue ELEMENT_ICD_HIT_STEP;     // every Nth hit (1, 1+N, 1+2N, ...)
    public static final IntValue ELEMENT_ICD_TIME_TICKS;   // time window ticks (e.g., 50 = 2.5s)

    // 以“modid：spell_id=amount”的形式覆盖;多个条目，以 ';' 分隔
    public static final ModConfigSpec.ConfigValue<String> ELEMENT_ATTACHMENT_OVERRIDES_TEXT;

    // 每个拼写元素附件以“modid：spell_id=element_id”的形式覆盖;多个条目，以 ';' 分隔
    public static final ModConfigSpec.ConfigValue<String> SPELL_ELEMENT_OVERRIDES_TEXT;

    // 武器相关配置
    public static final IntValue WEAPON_MAX_ELEMENT_AMOUNT;

    public static final ModConfigSpec SPEC;

    private static volatile Map<ResourceLocation, Integer> overridesCache;
    private static volatile Map<ResourceLocation, String> spellElementOverridesCache;

    static {
        BUILDER.push("element_attachment");
        ELEMENT_ATTACHMENT_DEFAULT = BUILDER
                .comment("没有显式覆盖的法术的默认元素附着量")
                .defineInRange("default", 200, 0, Integer.MAX_VALUE);

        ELEMENT_ICD_HIT_STEP = BUILDER
                .comment(
                        "ICD 攻击步长：允许在第 1、1+步长、1+2*步长... 次攻击时应用元素",
                        "设置为 3 表示允许第 1、4、7 次攻击等")
                .defineInRange("icd_hit_step", 3, 1, Integer.MAX_VALUE);

        ELEMENT_ICD_TIME_TICKS = BUILDER
                .comment(
                        "ICD 时间窗口（游戏刻）：如果上次应用时间超过此窗口则允许应用",
                        "50 刻 = 2.5 秒（20 TPS）")
                .defineInRange("icd_time_ticks", 50, 0, Integer.MAX_VALUE);

        ELEMENT_ATTACHMENT_OVERRIDES_TEXT = BUILDER
                .comment(
                        "单个法术覆盖，格式：modid:spell_id=数量",
                        "多个条目用逗号分隔",
                        "示例：irons_spellbooks:firebolt=250, irons_spellbooks:ice_shard=150",
                        "未知或格式错误的条目将被忽略")
                .define("overrides", "");

        SPELL_ELEMENT_OVERRIDES_TEXT = BUILDER
                .comment(
                        "单个法术元素附着覆盖，格式：modid:spell_id=元素ID",
                        "允许特定法术附着自定义元素而不是使用基于学派的映射",
                        "多个条目用逗号分隔",
                        "示例：irons_spellbooks:firebolt=lightning, irons_spellbooks:ice_shard=fire",
                        "元素ID应与 element_attachments 数据文件中定义的匹配",
                        "未知或格式错误的条目将被忽略")
                .define("spell_element_overrides", "");
        BUILDER.pop();

        BUILDER.push("weapon");
        WEAPON_MAX_ELEMENT_AMOUNT = BUILDER
                .comment("武器可附着的最大元素量")
                .defineInRange("max_element_amount", 20000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static int getSpellAttachmentAmount(ResourceLocation spellId) {
        Map<ResourceLocation, Integer> map = getOverridesCache();
        Integer v = map.get(spellId);
        return v != null ? v : ELEMENT_ATTACHMENT_DEFAULT.get();
    }

    private static Map<ResourceLocation, Integer> getOverridesCache() {
        Map<ResourceLocation, Integer> local = overridesCache;
        if (local != null) return local;
        synchronized (ServerConfig.class) {
            if (overridesCache == null) {
                overridesCache = parseOverrides(ELEMENT_ATTACHMENT_OVERRIDES_TEXT.get());
            }
            return overridesCache;
        }
    }

    public static void invalidateCache() {
        overridesCache = null;
        spellElementOverridesCache = null;
    }

    // ----- ICD getters -----
    public static int getIcdHitStep() {
        return ELEMENT_ICD_HIT_STEP.get();
    }

    public static int getIcdTimeTicks() {
        return ELEMENT_ICD_TIME_TICKS.get();
    }

    // ----- 武器配置获取方法 -----
    public static int getWeaponMaxElementAmount() {
        return WEAPON_MAX_ELEMENT_AMOUNT.get();
    }

    private static Map<ResourceLocation, Integer> parseOverrides(String text) {
        Map<ResourceLocation, Integer> map = new HashMap<>();
        if (text == null || text.isBlank()) return map;
        // Support separators: ';' ',' or newlines
        String normalized = text.replace('\n', ';').replace(',', ';');
        String[] parts = normalized.split(";");
        for (String raw : parts) {
            if (raw == null) continue;
            String s = raw.trim();
            if (s.isEmpty()) continue;
            int idx = s.indexOf('=');
            if (idx <= 0 || idx >= s.length() - 1) continue;
            String key = s.substring(0, idx).trim();
            String val = s.substring(idx + 1).trim();
            try {
                int amount = Integer.parseInt(val);
                if (amount < 0) continue;
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) {
                    map.put(id, amount);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    /**
     * 获取指定法术的自定义附着元素ID，如果没有配置则返回null
     */
    public static String getSpellElementOverride(ResourceLocation spellId) {
        Map<ResourceLocation, String> map = getSpellElementOverridesCache();
        return map.get(spellId);
    }

    private static Map<ResourceLocation, String> getSpellElementOverridesCache() {
        Map<ResourceLocation, String> local = spellElementOverridesCache;
        if (local != null) return local;
        synchronized (ServerConfig.class) {
            if (spellElementOverridesCache == null) {
                spellElementOverridesCache = parseSpellElementOverrides(SPELL_ELEMENT_OVERRIDES_TEXT.get());
            }
            return spellElementOverridesCache;
        }
    }

    private static Map<ResourceLocation, String> parseSpellElementOverrides(String text) {
        Map<ResourceLocation, String> map = new HashMap<>();
        if (text == null || text.isBlank()) return map;
        // Support separators: ';' ',' or newlines
        String normalized = text.replace('\n', ';').replace(',', ';');
        String[] parts = normalized.split(";");
        for (String raw : parts) {
            if (raw == null) continue;
            String s = raw.trim();
            if (s.isEmpty()) continue;
            int idx = s.indexOf('=');
            if (idx <= 0 || idx >= s.length() - 1) continue;
            String key = s.substring(0, idx).trim();
            String val = s.substring(idx + 1).trim();
            if (val.isEmpty()) continue;
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                map.put(id, val.toLowerCase()); // 元素ID统一小写
            }
        }
        return map;
    }
}
