package com.chadate.spellelemental.element.reaction.custom;

import com.chadate.spellelemental.element.reaction.registry.ElementReactionRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 元素反应管理器
 * 现在完全使用数据包驱动的反应系统
 */
public class ElementReactionManager {
    static boolean reactionApplied = false;

    /**
     * 处理元素反应
     * 现在完全依赖数据包驱动的反应系统
     */
    public void handleReaction(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        ElementReactionManager.reactionApplied = false;
        
        // 使用数据包驱动的反应系统
        boolean datapackReactionApplied = ElementReactionRegistry.handleReactions(event, attacker, astralBlessing);
        if (datapackReactionApplied) {
            ElementReactionManager.reactionApplied = true;
        }
    }

    public static boolean getrectionApplied(){
        return reactionApplied;
    }
}