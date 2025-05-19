package com.chadate.spellelemental;


import com.chadate.spellelemental.element.attachment.ElementAttachmentRegistry;
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

        ElementAttachmentRegistry.handleAttachment(target, source, entityId);
    }
}