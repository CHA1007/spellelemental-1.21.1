package com.chadate.spellelemental;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.element.reaction.ElementReactionManager;
import com.chadate.spellelemental.element.reaction.custom.fire.*;
import com.chadate.spellelemental.element.reaction.custom.ice.IceMeltReaction;
import com.chadate.spellelemental.element.reaction.custom.ice.IceSuperconductiveReaction;
import com.chadate.spellelemental.element.reaction.custom.lightning.*;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureBurnReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureDewSparkReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NatureGerminateReaction;
import com.chadate.spellelemental.element.reaction.custom.nature.NaturePromotionReaction;
import com.chadate.spellelemental.event.DamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ReactionEventHandler {
    private static final ElementReactionManager REACTION_MANAGER = new ElementReactionManager();

    static {
        // 只在类加载时注册一次反应
        registerReactions();
    }

    private static void registerReactions() {
        REACTION_MANAGER.register(new FirePromotionReaction());
        REACTION_MANAGER.register(new FireCombustignitionReaction());
        REACTION_MANAGER.register(new FireDeflagrationReaction());
        REACTION_MANAGER.register(new FireFreezeMeltReaction());
        REACTION_MANAGER.register(new FireEvaporateReaction());
        REACTION_MANAGER.register(new FireMeltReaction());
        REACTION_MANAGER.register(new FireBurnReaction());

        // lightning reactions
        REACTION_MANAGER.register(new LightningSurgechargeReaction());
        REACTION_MANAGER.register(new LightningSurgeReaction());
        REACTION_MANAGER.register(new LightningDeflagrationReaction());
        REACTION_MANAGER.register(new LightningFreezeVulnerableReaction());
        REACTION_MANAGER.register(new LightningElectroReaction());
        REACTION_MANAGER.register(new LightningSuperconductiveReaction());
        REACTION_MANAGER.register(new LightningPromotionReaction());

        // ice reactions
        REACTION_MANAGER.register(new IceSuperconductiveReaction());
        REACTION_MANAGER.register(new IceMeltReaction());

        // nature reactions
        REACTION_MANAGER.register(new NatureGerminateReaction());
        REACTION_MANAGER.register(new NatureBurnReaction());
        REACTION_MANAGER.register(new NatureDewSparkReaction());
        REACTION_MANAGER.register(new NaturePromotionReaction());
    }

    @SubscribeEvent
    public static void handleElementReactions(LivingDamageEvent.Pre event) {
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        float astralBlessing = 0;

        if (attacker != null) {
            astralBlessing = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
        }

        if (DamageEvent.IsSpellDamage(event)) {
            DamageEvent.CancelSpellUnbeatableFrames(target);
        }

        REACTION_MANAGER.handleReaction(event, attacker, astralBlessing);
    }
}