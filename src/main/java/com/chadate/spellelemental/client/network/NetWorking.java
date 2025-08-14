package com.chadate.spellelemental.client.network;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.client.network.custom.ClientPayloadHandler;
import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.server.network.ServerPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;

@EventBusSubscriber(modid = SpellElemental.MODID,bus = EventBusSubscriber.Bus.MOD)
public class NetWorking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SpellElemental.MODID)
                .executesOn(HandlerThread.NETWORK); // All subsequent payloads will register on the network thread
        registrar.playToClient(
                ElementData.TYPE,
                ElementData.STREAM_CODEC,
                new DirectionalPayloadHandler<ElementData>(
                        ClientPayloadHandler::handleDataOnNetwork,
                        null
                )
        );
        registrar.playToClient(
                ElementData.ElementSnapshot.TYPE,
                ElementData.ElementSnapshot.STREAM_CODEC,
                new DirectionalPayloadHandler<ElementData.ElementSnapshot>(
                        ClientPayloadHandler::handleSnapshotOnNetwork,
                        null
                )
        );
        registrar.playToClient(
                ElementData.ElementDebugToggle.TYPE,
                ElementData.ElementDebugToggle.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleDebugToggleOnNetwork(payload, context)
        );
        // 客户端 -> 服务端 检查请求
        registrar.playToServer(
                ElementData.ElementInspectRequest.TYPE,
                ElementData.ElementInspectRequest.STREAM_CODEC,
                (payload, context) -> ServerPayloadHandler.handleInspectRequest(payload, context)
        );
        // 服务端 -> 客户端 检查回包
        registrar.playToClient(
                ElementData.ElementInspectResponse.TYPE,
                ElementData.ElementInspectResponse.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.handleInspectResponseOnNetwork(payload, context)
        );
    }
}