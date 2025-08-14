package com.chadate.spellelemental.element.attachment.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UnifiedElementAttachmentAssets {
	private static final Map<String, String> ELEMENT_ID_TO_ICON = new ConcurrentHashMap<>();

	private UnifiedElementAttachmentAssets() {}

	public static void clear() {
		ELEMENT_ID_TO_ICON.clear();
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

	private static String toBaseKey(String key) {
		if (key == null) return "";
		int idx = key.indexOf(':');
		String s = idx >= 0 ? key.substring(idx + 1) : key;
		return s;
	}
} 