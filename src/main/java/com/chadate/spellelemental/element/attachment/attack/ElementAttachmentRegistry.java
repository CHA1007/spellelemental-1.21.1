package com.chadate.spellelemental.element.attachment.attack;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    private static final AtomicReference<String> latestAppliedElement = new AtomicReference<>("");

    public static String getLatestAppliedElement() {
        return latestAppliedElement.get();
    }
}