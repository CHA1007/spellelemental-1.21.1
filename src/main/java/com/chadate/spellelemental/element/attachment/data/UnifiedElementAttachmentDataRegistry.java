package com.chadate.spellelemental.element.attachment.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 统一元素附着数据注册类
 * 负责注册统一的元素附着数据加载器
 */
public class UnifiedElementAttachmentDataRegistry {
    
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new UnifiedElementAttachmentDataLoader());
    }
}
