package com.chadate.spellelemental.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.*;

public class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Default element attachment amount applied to any spell without an explicit override
    public static final IntValue ELEMENT_ATTACHMENT_DEFAULT;

    // Overrides in the form "modid:spell_id=amount"; multiple entries separated by ';'
    public static final ModConfigSpec.ConfigValue<String> ELEMENT_ATTACHMENT_OVERRIDES_TEXT;

    // Per-spell element attachment overrides in the form "modid:spell_id=element_id"; multiple entries separated by ';'
    public static final ModConfigSpec.ConfigValue<String> SPELL_ELEMENT_OVERRIDES_TEXT;

    public static final ModConfigSpec SPEC;

    private static volatile Map<ResourceLocation, Integer> overridesCache;
    private static volatile Map<ResourceLocation, String> spellElementOverridesCache;

    static {
        BUILDER.push("element_attachment");
        ELEMENT_ATTACHMENT_DEFAULT = BUILDER
                .comment("Default element attachment amount for spells without explicit override")
                .defineInRange("default", 200, 0, Integer.MAX_VALUE);

        ELEMENT_ATTACHMENT_OVERRIDES_TEXT = BUILDER
                .comment(
                        "Per-spell overrides in the format modid:spell_id=amount.",
                        "Multiple entries separated by comma.",
                        "Example: irons_spellbooks:firebolt=250, irons_spellbooks:ice_shard=150",
                        "Unknown or malformed entries will be ignored.")
                .define("overrides", "");

        SPELL_ELEMENT_OVERRIDES_TEXT = BUILDER
                .comment(
                        "Per-spell element attachment overrides in the format modid:spell_id=element_id.",
                        "This allows specific spells to attach custom elements instead of using school-based mapping.",
                        "Multiple entries separated by comma.",
                        "Example: irons_spellbooks:firebolt=lightning, irons_spellbooks:ice_shard=fire",
                        "Element IDs should match those defined in element_attachments data files.",
                        "Unknown or malformed entries will be ignored.")
                .define("spell_element_overrides", "");
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
