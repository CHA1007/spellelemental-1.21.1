package com.chadate.spellelemental.server.network;

import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public final class ServerPayloadHandler {
	private ServerPayloadHandler() {}

	public static void handleInspectRequest(final ElementData.ElementInspectRequest payload, final IPayloadContext context) {
		context.enqueueWork(() -> {
			Entity target = context.player().level().getEntity(payload.entityId);
			if (!(target instanceof LivingEntity living)) return;
			ElementContainerAttachment container = living.getData(SpellAttachments.ELEMENTS_CONTAINER);
			Map<String, Integer> snap = container.snapshot();
			String[] keys = snap.keySet().toArray(new String[0]);
			int[] values = new int[keys.length];
			for (int i = 0; i < keys.length; i++) values[i] = snap.get(keys[i]);
			PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ElementData.ElementInspectResponse(living.getId(), keys, values));
		});
	}
} 