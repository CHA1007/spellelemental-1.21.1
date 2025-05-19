package com.chadate.spellelemental.element.reaction;

import com.chadate.spellelemental.element.reaction.custom.fire.*;
import com.chadate.spellelemental.element.reaction.custom.ice.IceMeltReaction;
import com.chadate.spellelemental.element.reaction.custom.ice.IceSuperconductiveReaction;
import com.chadate.spellelemental.element.reaction.custom.lightning.*;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureBurnReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureDewSparkReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureGerminateReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NaturePromotionReaction;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ElementReactionManager {
    private final List<ElementReaction> reactions = new ArrayList<>();

    public void register(ElementReaction reaction) {
        reactions.add(reaction);
    }

    // 使用优先级排序确保关键反应先触发
    public void handleReaction(LivingDamageEvent.Pre event, LivingEntity attacker, float astralBlessing) {
        List<ElementReaction> sortedReactions = new ArrayList<>(reactions);
        sortedReactions.sort(Comparator.comparingInt(this::getPriority).reversed());

        for (ElementReaction reaction : sortedReactions) {
            if (reaction.appliesTo(event.getEntity(), event.getSource())) {
                reaction.apply(event, attacker, astralBlessing);
            }
        }
    }

    // 定义反应优先级
    private int getPriority(ElementReaction reaction) {
        //fire reaction
        if (reaction instanceof FirePromotionReaction) return 100;
        if (reaction instanceof FireCombustignitionReaction) return 99;
        if (reaction instanceof FireDeflagrationReaction) return 98;
        if (reaction instanceof FireFreezeMeltReaction) return 97;
        if (reaction instanceof FireEvaporateReaction) return 96;
        if (reaction instanceof FireMeltReaction) return 95;
        if (reaction instanceof FireBurnReaction) return 94;

        //lightning reaction
        if (reaction instanceof LightningSurgechargeReaction) return 93;
        if (reaction instanceof LightningPromotionReaction) return 92;
        if (reaction instanceof LightningSurgeReaction) return 91;
        if (reaction instanceof LightningDeflagrationReaction) return 90;
        if (reaction instanceof LightningFreezeVulnerableReaction) return 89;
        if (reaction instanceof LightningElectroReaction) return 88;
        if (reaction instanceof LightningSuperconductiveReaction) return 87;

        //ice reaction
        if (reaction instanceof IceSuperconductiveReaction) return 86;
        if (reaction instanceof IceMeltReaction) return 85;

        //nature reaction
        if (reaction instanceof NaturePromotionReaction) return 84;
        if (reaction instanceof NatureGerminateReaction) return 83;
        if (reaction instanceof NatureBurnReaction) return 82;
        if (reaction instanceof NatureDewSparkReaction) return 81;
        return 0;
    }
}