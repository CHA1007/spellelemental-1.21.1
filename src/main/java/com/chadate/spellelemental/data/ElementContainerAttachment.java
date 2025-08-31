package com.chadate.spellelemental.data;

import java.util.HashMap;
import java.util.Map;

public class ElementContainerAttachment {
    private final Map<String, Integer> elementIdToValue = new HashMap<>();
    private final Map<String, Long> lastAppliedGameTime = new HashMap<>();
    private final Map<String, Integer> lastAttackerIds = new HashMap<>();

    public int getValue(String elementId) {
        if (elementId == null) return 0;
        return elementIdToValue.getOrDefault(elementId.toLowerCase(), 0);
    }

    public void setValue(String elementId, int value) {
        if (elementId == null) return;
        String key = elementId.toLowerCase();
        if (value <= 0) {
            elementIdToValue.remove(key);
        } else {
            elementIdToValue.put(key, value);
        }
    }

    /** 明确记录最近一次附着的游戏时间（毫秒/刻度按 level.getGameTime 提供的 long）。*/
    public void markApplied(String elementId, long gameTime) {
        if (elementId == null) return;
        lastAppliedGameTime.put(elementId.toLowerCase(), gameTime);
    }

    /** 记录最近一次附着该元素的攻击者ID和时间 */
    public void markAppliedWithAttacker(String elementId, long gameTime, int attackerId) {
        if (elementId == null) return;
        String key = elementId.toLowerCase();
        lastAppliedGameTime.put(key, gameTime);
        lastAttackerIds.put(key, attackerId);
    }

    /** 获取最近一次附着的时间；未记录则返回 0。*/
    public long getLastApplied(String elementId) {
        if (elementId == null) return 0L;
        return lastAppliedGameTime.getOrDefault(elementId.toLowerCase(), 0L);
    }

    /** 获取最近一次附着该元素的攻击者ID；未记录则返回 -1 */
    public int getLastAttackerId(String elementId) {
        if (elementId == null) return -1;
        return lastAttackerIds.getOrDefault(elementId.toLowerCase(), -1);
    }

    public void remove(String elementId) {
        if (elementId == null) return;
        String key = elementId.toLowerCase();
        elementIdToValue.remove(key);
        // 可选择是否清理时间戳，这里保留，方便做“最近一次附着但已清除”的判定需要时拓展
        // lastAppliedGameTime.remove(key);
    }

    public Map<String, Integer> snapshot() {
        return new HashMap<>(elementIdToValue);
    }

    /** 提供时间戳快照（仅服务端内部调试/算法使用，不做网络同步）。*/
    public Map<String, Long> snapshotLastApplied() {
        return new HashMap<>(lastAppliedGameTime);
    }
}