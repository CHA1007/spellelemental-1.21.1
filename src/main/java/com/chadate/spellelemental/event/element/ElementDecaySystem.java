package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.network.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
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

    public static void elementDecay(ServerTickEvent.Post event) {
        event.getServer().overworld();
        if (TRACKED.isEmpty()) return;

        TRACKED.removeIf(entity -> entity == null || !entity.isAlive() || entity.level().isClientSide());
        for (LivingEntity entity : TRACKED) {
            ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
            Map<String, Integer> snap = container.snapshot();
            if (snap.isEmpty()) continue;
            for (Map.Entry<String, Integer> e : snap.entrySet()) {
                String key = e.getKey();
                int value = e.getValue();
                if (value <= 0) continue;
                value -= 1;
                container.setValue(key, value);
                // 强制将所有元素附着状态都发送给所有玩家
                PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), key, value));
            }
        }
    }
}