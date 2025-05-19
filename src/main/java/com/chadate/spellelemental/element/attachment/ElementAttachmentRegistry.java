package com.chadate.spellelemental.element.attachment;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class ElementAttachmentRegistry {
    private static final List<ElementAttachmentHandler> handlers = new ArrayList<>();

    public static void register(ElementAttachmentHandler handler) {
        handlers.add(handler);
    }

    public static void handleAttachment(LivingEntity target, DamageSource source, int entityId) {
        for (ElementAttachmentHandler handler : handlers) {
            if (handler.canApply(target, source)) {
                handler.applyEffect(target, source, entityId);
                break;
            }
        }
    }
}