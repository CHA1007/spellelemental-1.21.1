package com.chadate.spellelemental.element.attachment.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UnifiedElementAttachmentAssets {
    private static final Map<String, String> ELEMENT_ID_TO_ICON = new ConcurrentHashMap<>();
    private static final Map<String, String> ELEMENT_ID_TO_PARTICLE = new ConcurrentHashMap<>();

    private UnifiedElementAttachmentAssets() {}

    public static void clear() {
        ELEMENT_ID_TO_ICON.clear();
        ELEMENT_ID_TO_PARTICLE.clear();
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

    private static String toBaseKey(String key) {
        if (key == null) return "";
        int idx = key.indexOf(':');
        String s = idx >= 0 ? key.substring(idx + 1) : key;
        return s;
    }
}