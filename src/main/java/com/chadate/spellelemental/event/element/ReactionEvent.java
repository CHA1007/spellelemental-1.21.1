package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class ReactionEvent {
	private static String toContainerKey(String base) {
		if (base == null) return "";
		// 与 DynamicElementHandler.extractElementKey() 保持一致的逻辑
		String s = base.contains(":") ? 
			base.substring(base.indexOf(":") + 1) : 
			base;
		return s.toLowerCase();
	}

	public static void setElementAttachment(LivingEntity entity, String element, int value) {
		ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
		String key = toContainerKey(element);
		container.setValue(key, value);

		// 同步到客户端（0 表示移除）
		PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), key, value));
	}

	public static int getElementAttachment(LivingEntity entity, String element) {
		ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
		return container.getValue(toContainerKey(element));
	}
}
