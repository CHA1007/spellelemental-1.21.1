package com.chadate.spellelemental.element.reaction.custom;

import com.chadate.spellelemental.element.reaction.basic.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ElementReactionManager {
    private static final List<ElementReaction> reactions = new ArrayList<>();

    public void register(ElementReaction reaction) {
        reactions.add(reaction);
    }
    static boolean reactionApplied = false;

    // 使用优先级排序确保关键反应先触发
    public void handleReaction(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        List<ElementReaction> sortedReactions = new ArrayList<>(reactions);
        sortedReactions.sort(Comparator.comparingInt(this::getPriority));

        ElementReactionManager.reactionApplied = false;
        for (ElementReaction reaction : sortedReactions) {
            if (reaction.appliesTo(event.getEntity(), event.getSource())) {
                reaction.apply(event, attacker, astralBlessing);
                ElementReactionManager.reactionApplied = true;
            }
        }
    }

    public static boolean getrectionApplied(){
        return reactionApplied;
    }
    // 定义反应优先级
    private int getPriority(ElementReaction reaction) {
        if (reaction instanceof Vaporize) return 1;
        if (reaction instanceof Melt) return 2;
        if (reaction instanceof Overload) return 3;
        if (reaction instanceof ElectroChargedDamage) return 4;
        if (reaction instanceof Superconduct) return 5;
        if (reaction instanceof Shatter) return 6;
        if (reaction instanceof Promotion) return 7;
        if (reaction instanceof TriggerPromotion) return 8;
        if (reaction instanceof DewSpark) return 9;
        if (reaction instanceof TriggerDewSpark) return 10;
        if (reaction instanceof BurningDamage) return 11;

        return 0;
    }
}