package com.chadate.spellelemental.element.attachment.data;

import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EnvironmentalAttachmentRegistry {
	private static final List<UnifiedElementAttachmentConfig> ENV_CONFIGS = new ArrayList<>();

	private EnvironmentalAttachmentRegistry() {}

	public static void clear() { ENV_CONFIGS.clear(); }

	public static void add(UnifiedElementAttachmentConfig config) { if (config != null) ENV_CONFIGS.add(config); }

	public static List<UnifiedElementAttachmentConfig> getAll() { return Collections.unmodifiableList(ENV_CONFIGS); }
} 