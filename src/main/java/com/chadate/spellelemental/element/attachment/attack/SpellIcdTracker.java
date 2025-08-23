package com.chadate.spellelemental.element.attachment.attack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 跟踪每个攻击者+法术的法术元素应用的内部冷却时间 （ICD）。
 * 规则：允许在任一情况下申请
 * - 命中计数器允许：每 N 个序列 （1， 1+N， 1+2N， ...），默认 N=3
 * - 时间窗口允许：上次应用早于 T 刻度，默认 T=50 （2.5s）
 * 申请成功后，计数器重置和 lastApplyTick 更新。
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

    /** 在每个法术伤害事件中调用此值以记录命中并检查余量 */
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
