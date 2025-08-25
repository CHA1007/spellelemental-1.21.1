package com.chadate.spellelemental.element.attachment.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UnifiedElementAttachmentAssets {
    private static final Map<String, String> ELEMENT_ID_TO_ICON = new ConcurrentHashMap<>();
    private static final Map<String, String> ELEMENT_ID_TO_PARTICLE = new ConcurrentHashMap<>();
    private static final Map<String, String> ELEMENT_ID_TO_SCHOOL = new ConcurrentHashMap<>();

    private UnifiedElementAttachmentAssets() {}

    public static void clear() {
        ELEMENT_ID_TO_ICON.clear();
        ELEMENT_ID_TO_PARTICLE.clear();
        ELEMENT_ID_TO_SCHOOL.clear();
    }

    public static void setIcon(String attachmentTypeOrKey, String iconResource) {
        if (attachmentTypeOrKey == null || attachmentTypeOrKey.isEmpty() || iconResource == null || iconResource.isEmpty()) return;
        String key = attachmentTypeOrKey.toLowerCase();
        ELEMENT_ID_TO_ICON.put(key, iconResource);
        String base = toBaseKey(key);
        ELEMENT_ID_TO_ICON.put(base, iconResource);
    }

    public static String getIcon(String elementKeyOrAttachmentType) {
        if (elementKeyOrAttachmentType == null) return null;
        String key = elementKeyOrAttachmentType.toLowerCase();
        String icon = ELEMENT_ID_TO_ICON.get(key);
        if (icon != null) return icon;
        return ELEMENT_ID_TO_ICON.get(toBaseKey(key));
    }

    public static void setParticleEffect(String attachmentTypeOrKey, String particleKey) {
        if (attachmentTypeOrKey == null || attachmentTypeOrKey.isEmpty() || particleKey == null || particleKey.isEmpty()) return;
        String key = attachmentTypeOrKey.toLowerCase();
        ELEMENT_ID_TO_PARTICLE.put(key, particleKey);
        String base = toBaseKey(key);
        ELEMENT_ID_TO_PARTICLE.put(base, particleKey);
    }

    public static String getParticleEffect(String elementKeyOrAttachmentType) {
        if (elementKeyOrAttachmentType == null) return null;
        String key = elementKeyOrAttachmentType.toLowerCase();
        String effect = ELEMENT_ID_TO_PARTICLE.get(key);
        if (effect != null) return effect;
        return ELEMENT_ID_TO_PARTICLE.get(toBaseKey(key));
    }

    public static void setSchool(String elementIdOrKey, String school) {
        if (elementIdOrKey == null || elementIdOrKey.isEmpty() || school == null || school.isEmpty()) return;
        String key = elementIdOrKey.toLowerCase();
        ELEMENT_ID_TO_SCHOOL.put(key, school);
        ELEMENT_ID_TO_SCHOOL.put(toBaseKey(key), school);
    }

    public static String getSchool(String elementKeyOrAttachmentType) {
        if (elementKeyOrAttachmentType == null) return null;
        String key = elementKeyOrAttachmentType.toLowerCase();
        String s = ELEMENT_ID_TO_SCHOOL.get(key);
        if (s != null) return s;
        return ELEMENT_ID_TO_SCHOOL.get(toBaseKey(key));
    }

    /**
     * 根据学派名查找一个已注册的元素ID（返回首个匹配项）。
     * 学派与元素ID均大小写不敏感。
     */
    public static String getElementIdBySchool(String schoolName) {
        if (schoolName == null || schoolName.isEmpty()) return null;
        String target = schoolName.toLowerCase();
        for (Map.Entry<String, String> e : ELEMENT_ID_TO_SCHOOL.entrySet()) {
            String s = e.getValue();
            if (s != null && s.equalsIgnoreCase(target)) {
                return toBaseKey(e.getKey());
            }
        }
        return null;
    }

    /**
     * 获取所有图标映射（用于网络同步）
     */
    public static Map<String, String> getAllIcons() {
        return Map.copyOf(ELEMENT_ID_TO_ICON);
    }

    /**
     * 获取所有粒子效果映射（用于网络同步）
     */
    public static Map<String, String> getAllParticleEffects() {
        return Map.copyOf(ELEMENT_ID_TO_PARTICLE);
    }

    /**
     * 获取所有学派映射（用于网络同步）
     */
    public static Map<String, String> getAllSchools() {
        return Map.copyOf(ELEMENT_ID_TO_SCHOOL);
    }

    private static String toBaseKey(String key) {
        if (key == null) return "";
        int idx = key.indexOf(':');
        String s = idx >= 0 ? key.substring(idx + 1) : key;
        return s;
    }
}