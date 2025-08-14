package com.chadate.spellelemental.element.reaction.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 元素反应数据注册
 * 在数据重载时注册元素反应数据加载器
 */
@EventBusSubscriber
public class ElementReactionDataRegistry {

	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener(new ElementReactionDataLoader());
	}
} 