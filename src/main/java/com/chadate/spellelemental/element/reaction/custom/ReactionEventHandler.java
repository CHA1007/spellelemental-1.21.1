package com.chadate.spellelemental.element.reaction.custom;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.element.reaction.basic.*;
import com.chadate.spellelemental.element.reaction.effect.Hemopyre;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ReactionEventHandler {
    private static final ElementReactionManager REACTION_MANAGER = new ElementReactionManager();

    static {
        registerReactions();
    }

    private static void registerReactions() {
        REACTION_MANAGER.register(new Vaporize());
        REACTION_MANAGER.register(new Melt());
        REACTION_MANAGER.register(new Overload());
        REACTION_MANAGER.register(new ElectroChargedDamage());
        REACTION_MANAGER.register(new Superconduct());
        REACTION_MANAGER.register(new Shatter());
        REACTION_MANAGER.register(new Promotion());
        REACTION_MANAGER.register(new TriggerPromotion());
        REACTION_MANAGER.register(new DewSpark());
        REACTION_MANAGER.register(new TriggerDewSpark());
        REACTION_MANAGER.register(new BurningDamage());
        REACTION_MANAGER.register(new Hemopyre());
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