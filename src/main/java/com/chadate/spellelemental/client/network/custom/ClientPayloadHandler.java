package com.chadate.spellelemental.client.network.custom;

import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class ClientPayloadHandler {
	public static void handleDataOnNetwork(final ElementData data, final IPayloadContext context) {
		String element = data.elementMessage;
		int entityId = data.entityIdMessage;
		int elementDuration = data.durationMessage;

		context.enqueueWork(() -> {
			Entity entity = context.player().level().getEntity(entityId);
			if (entity == null) return;

			ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);

			if (elementDuration == 0) {
				container.remove(element);
				DisplayCache.remove(entityId, element);
			} else {
				container.setValue(element, elementDuration);
				long now = context.player().level().getGameTime();
				DisplayCache.update(entityId, element, elementDuration, now);
			}
		});
	}

	public static void handleSnapshotOnNetwork(final ElementData.ElementSnapshot snapshot, final IPayloadContext context) {
		context.enqueueWork(() -> {
			Entity entity = context.player().level().getEntity(snapshot.entityId);
			if (entity == null) return;
			ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
			// 清空并重建
			container.snapshot().keySet().forEach(container::remove);
			DisplayCache.clearEntity(snapshot.entityId);
			long now = context.player().level().getGameTime();
			for (int i = 0; i < snapshot.keys.length; i++) {
				int v = snapshot.values[i];
				if (v > 0) {
					container.setValue(snapshot.keys[i], v);
					DisplayCache.update(snapshot.entityId, snapshot.keys[i], v, now);
				} else {
					container.remove(snapshot.keys[i]);
				}
			}
		});
	}

	public static void handleDebugToggleOnNetwork(final ElementData.ElementDebugToggle payload, final IPayloadContext context) {
		context.enqueueWork(() -> DebugState.enabled = payload.enabled);
	}

	public static void handleInspectResponseOnNetwork(final ElementData.ElementInspectResponse resp, final IPayloadContext context) {
		context.enqueueWork(() -> {
			InspectCache.entityId = resp.entityId;
			InspectCache.data.clear();
			for (int i = 0; i < resp.keys.length; i++) InspectCache.data.put(resp.keys[i], resp.values[i]);
			InspectCache.lastUpdateMs = System.currentTimeMillis();
		});
	}

	public static final class DebugState {
		public static boolean enabled = false;
	}

	public static final class InspectCache {
		public static int entityId = -1;
		public static final Map<String, Integer> data = new HashMap<>();
		public static long lastUpdateMs = 0L;
	}

	// 显示用的本地预测缓存，便于渐变/闪烁计算
	public static final class DisplayCache {
		private static final Map<Integer, Map<String, Status>> CACHE = new HashMap<>();
		public static void update(int entityId, String key, int duration, long gameTime) {
			CACHE.computeIfAbsent(entityId, k -> new HashMap<>()).put(key, new Status(duration, gameTime));
		}
		public static void remove(int entityId, String key) {
			Map<String, Status> m = CACHE.get(entityId);
			if (m != null) m.remove(key);
		}
		public static void clearEntity(int entityId) { CACHE.remove(entityId); }
		public static int predictRemaining(int entityId, String key) {
			Map<String, Status> m = CACHE.get(entityId);
			if (m == null) return -1;
			Status s = m.get(key);
			if (s == null) return -1;
			long now = (Minecraft.getInstance().level != null) ? Minecraft.getInstance().level.getGameTime() : s.atGameTime;
			long elapsed = Math.max(0, now - s.atGameTime);
			long remain = s.startDuration - elapsed;
			return (int)Math.max(0, remain);
		}

        private record Status(int startDuration, long atGameTime) {
        }
	}
}