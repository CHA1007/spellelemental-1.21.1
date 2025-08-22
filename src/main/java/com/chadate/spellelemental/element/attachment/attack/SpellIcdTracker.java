package com.chadate.spellelemental.element.attachment.attack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks Internal Cooldown (ICD) for spell elemental application per attacker+spell.
 * Rule: allow application when either
 *  - hit counter allows: every Nth sequence (1, 1+N, 1+2N, ...), default N=3
 *  - time window allows: last application older than T ticks, default T=50 (2.5s)
 * After a successful application, counter resets and lastApplyTick updates.
 */
public final class SpellIcdTracker {
    private SpellIcdTracker() {}

    private static final class Key {
        private final int attackerId;
        private final int targetId;
        private final String spellKey;
        Key(int attackerId, int targetId, ResourceLocation spellId) {
            this.attackerId = attackerId;
            this.targetId = targetId;
            this.spellKey = spellId == null ? "" : spellId.toString();
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return attackerId == key.attackerId && targetId == key.targetId && Objects.equals(spellKey, key.spellKey);
        }
        @Override public int hashCode() { return Objects.hash(attackerId, targetId, spellKey); }
    }

    private static final class State {
        final AtomicInteger hitsSinceLastApply = new AtomicInteger(0);
        volatile long lastApplyTick = Long.MIN_VALUE; // no apply yet
    }

    private static final Map<Key, State> STATES = new ConcurrentHashMap<>();

    /** Call this on every spell damage event to record a hit and check allowance. */
    public static boolean allowAndRecord(Entity attacker, Entity target, ResourceLocation spellId, long currentTick,
                                         int step, int timeWindowTicks) {
        int attackerId = attacker == null ? -1 : attacker.getId();
        int targetId = target == null ? -1 : target.getId();
        Key key = new Key(attackerId, targetId, spellId);
        State state = STATES.computeIfAbsent(key, k -> new State());

        int hits = state.hitsSinceLastApply.incrementAndGet();
        int s = Math.max(1, step);
        boolean hitGate = ((hits - 1) % s) == 0; // 1, 1+s, ...
        boolean timeGate = (state.lastApplyTick == Long.MIN_VALUE) ||
                (currentTick - state.lastApplyTick) >= Math.max(0, timeWindowTicks);

        boolean allow = hitGate || timeGate;
        if (allow) {
            state.lastApplyTick = currentTick;
            // 不重置命中计数，确保仅在 1, 1+step, 1+2*step ... 放行
            // 如果希望时间窗放行不改变序列，同样不重置计数
        }
        return allow;
    }

}
