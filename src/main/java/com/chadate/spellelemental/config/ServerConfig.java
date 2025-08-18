package com.chadate.spellelemental.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.*;

public class ServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Default element attachment amount applied to any spell without an explicit override
    public static final IntValue ELEMENT_ATTACHMENT_DEFAULT;

    // Overrides in the form "modid:spell_id=amount"
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ELEMENT_ATTACHMENT_OVERRIDES;

    public static final ModConfigSpec SPEC;

    private static volatile Map<ResourceLocation, Integer> overridesCache;

    static {
        BUILDER.push("element_attachment");
        ELEMENT_ATTACHMENT_DEFAULT = BUILDER
                .comment("Default element attachment amount for spells without explicit override")
                .defineInRange("default", 200, 0, Integer.MAX_VALUE);

        ELEMENT_ATTACHMENT_OVERRIDES = BUILDER
                .comment(
                        "Per-spell overrides in the format 'modid:spell_id=amount'.",
                        "Example: ironsspellbooks:firebolt=250",
                        "Unknown or malformed entries will be ignored.")
                .defineList("overrides", Collections.emptyList(),
                        o -> o instanceof String && ((String) o).contains("=") && ((String) o).indexOf('=') > 0);
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
                overridesCache = parseOverrides(ELEMENT_ATTACHMENT_OVERRIDES.get());
            }
            return overridesCache;
        }
    }

    public static void invalidateCache() {
        overridesCache = null;
    }

    private static Map<ResourceLocation, Integer> parseOverrides(List<? extends String> entries) {
        Map<ResourceLocation, Integer> map = new HashMap<>();
        if (entries == null) return map;
        for (String s : entries) {
            if (s == null) continue;
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
}
