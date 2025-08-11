package com.chadate.spellelemental.element.attachment.environmental.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 环境条件元素附着数据注册类
 * 负责注册数据加载器和事件处理器
 */
@EventBusSubscriber
public class EnvironmentalAttachmentDataRegistry {
    
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EnvironmentalAttachmentDataLoader());
    }
    
    /**
     * 注册环境条件事件处理器
     * 在模组初始化时调用
     */
    public static void registerEventHandlers() {
        // 事件处理器通过@EventBusSubscriber自动注册
        // 这里可以添加其他初始化逻辑
    }
}
