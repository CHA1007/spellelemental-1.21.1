package com.chadate.spellelemental.client.network;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.client.network.custom.ClientPayloadHandler;
import com.chadate.spellelemental.network.ElementData;
import com.chadate.spellelemental.server.network.ServerPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SpellElemental.MODID)
public class NetWorking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SpellElemental.MODID)
                .executesOn(HandlerThread.NETWORK);
        registrar.playToClient(
                ElementData.TYPE,
                ElementData.STREAM_CODEC,
                ClientPayloadHandler::handleDataOnNetwork
        );
        registrar.playToClient(
                ElementData.ElementSnapshot.TYPE,
                ElementData.ElementSnapshot.STREAM_CODEC,
                ClientPayloadHandler::handleSnapshotOnNetwork
        );
        registrar.playToClient(
                ElementData.ElementDebugToggle.TYPE,
                ElementData.ElementDebugToggle.STREAM_CODEC,
                ClientPayloadHandler::handleDebugToggleOnNetwork
        );
        // 客户端 -> 服务端 检查请求
        registrar.playToServer(
                ElementData.ElementInspectRequest.TYPE,
                ElementData.ElementInspectRequest.STREAM_CODEC,
                ServerPayloadHandler::handleInspectRequest
        );
        // 服务端 -> 客户端 检查回包
        registrar.playToClient(
                ElementData.ElementInspectResponse.TYPE,
                ElementData.ElementInspectResponse.STREAM_CODEC,
                ClientPayloadHandler::handleInspectResponseOnNetwork
        );
    }
}