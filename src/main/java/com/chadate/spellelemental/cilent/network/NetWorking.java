package com.chadate.spellelemental.cilent.network;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.cilent.network.custom.ClientPayloadHandler;
import com.chadate.spellelemental.cilent.network.custom.ElementData;
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
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleDataOnNetwork,
                        null
                )
        );
    }
}