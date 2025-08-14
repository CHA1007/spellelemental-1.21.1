package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class ElementDecaySystem {
	private static final Set<LivingEntity> TRACKED = Collections.newSetFromMap(new WeakHashMap<>());

	private ElementDecaySystem() {}

	public static void track(LivingEntity entity) {
		if (entity != null && !entity.level().isClientSide()) {
			TRACKED.add(entity);
		}
	}

	public static void onServerTick(ServerTickEvent.Post event) {
		if (!(event.getServer().overworld() instanceof ServerLevel)) return;
		if (TRACKED.isEmpty()) return;

		TRACKED.removeIf(entity -> entity == null || !entity.isAlive() || entity.level().isClientSide());
		for (LivingEntity entity : TRACKED) {
			ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
			Map<String, Integer> snap = container.snapshot();
			if (snap.isEmpty()) continue;
			boolean hasAny = false;
			for (Map.Entry<String, Integer> e : snap.entrySet()) {
				String key = e.getKey();
				int value = e.getValue();
				if (value <= 0) continue;
				int newValue = value - 1;
				container.setValue(key, newValue);
				if (newValue == 0) {
					PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), key, 0));
				} else {
					hasAny = true;
				}
			}
			if (!hasAny) {
				// 容器已清空，从跟踪集合移除
				// 延迟由外层 removeIf 处理
			}
		}
	}
} 