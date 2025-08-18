package com.chadate.spellelemental.element.reaction.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 最小元素反应配置，仅包含唯一ID。
 */
public class ElementReactionConfig {
    @SerializedName("reaction_id")
    private String reactionId;
    /**
     * 触发类型："damage" 或 "tick"
     */
    @SerializedName("trigger_type")
    private String triggerType;
    /**
     * 新结构：无序组合数组，示例：["ice", "fire"]
     */
    @SerializedName("elements")
    private List<String> elements;
    /**
     * 新结构：有序组合二维数组，示例：[["fire","ice"],["ice","fire"]]
     */
    @SerializedName("ordered")
    private List<List<String>> ordered;

    public String getReactionId() {
        return reactionId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public List<String> getElements() {
        return elements;
    }

    public List<List<String>> getOrdered() {
        return ordered;
    }
}
