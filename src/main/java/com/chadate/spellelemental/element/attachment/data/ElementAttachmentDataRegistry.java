package com.chadate.spellelemental.element.attachment.data;

import com.chadate.spellelemental.SpellElemental;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 数据加载器注册类
 * 负责在服务器启动时注册元素附着数据加载器
 */
@EventBusSubscriber(modid = SpellElemental.MODID)
public class ElementAttachmentDataRegistry {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ElementAttachmentDataLoader());
        SpellElemental.LOGGER.info("Registered ElementAttachmentDataLoader");
    }
}
