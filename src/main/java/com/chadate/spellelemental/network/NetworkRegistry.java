package com.chadate.spellelemental.network;

import com.chadate.spellelemental.SpellElemental;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络数据包注册类
 * 负责注册所有自定义网络数据包
 */
@EventBusSubscriber(modid = SpellElemental.MODID)
public class NetworkRegistry {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        // 注册元素附着同步数据包
        registrar.playToClient(
            ElementAttachmentSyncPacket.TYPE,
            ElementAttachmentSyncPacket.STREAM_CODEC,
            ElementAttachmentSyncPacket::handleClient
        );
    }
}
