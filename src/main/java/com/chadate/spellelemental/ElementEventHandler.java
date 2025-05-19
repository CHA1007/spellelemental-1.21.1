package com.chadate.spellelemental;


import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.element.attachment.ElementAttachmentRegistry;
import com.chadate.spellelemental.element.reaction.ElementReactionManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ElementEventHandler {
    @SubscribeEvent
    public static void handleElementAttachment(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        int entityId = target.getId();

        // 处理元素反应并返回是否触发
        boolean reactionOccurred = ElementReactionManager.getrectionApplied();
        System.out.println(reactionOccurred);
        // 如果没有发生元素反应，才执行元素附着
        if (!reactionOccurred) {
            ElementAttachmentRegistry.handleAttachment(target, source, entityId);
        }
    }
}