package com.chadate.spellelemental.data;

import java.util.HashMap;
import java.util.Map;

public class ElementContainerAttachment {
	private final Map<String, Integer> elementIdToValue = new HashMap<>();

	public int getValue(String elementId) {
		if (elementId == null) return 0;
		return elementIdToValue.getOrDefault(elementId.toLowerCase(), 0);
	}

	public void setValue(String elementId, int value) {
		if (elementId == null) return;
		String key = elementId.toLowerCase();
		if (value <= 0) {
			elementIdToValue.remove(key);
		} else {
			elementIdToValue.put(key, value);
		}
	}

	public void remove(String elementId) {
		if (elementId == null) return;
		elementIdToValue.remove(elementId.toLowerCase());
	}

	public Map<String, Integer> snapshot() {
		return new HashMap<>(elementIdToValue);
	}
} 