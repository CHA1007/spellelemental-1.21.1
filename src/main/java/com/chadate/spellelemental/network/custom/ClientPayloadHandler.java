package com.chadate.spellelemental.network.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.custom.ElementsAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.attachment.AttachmentType;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class ClientPayloadHandler {
    private static final Map<String, Supplier<AttachmentType<ElementsAttachment>>> ELEMENT_MAP = new HashMap<>();
    static {
        ELEMENT_MAP.put("fire_element", SpellAttachments.FIRE_ELEMENT);
        ELEMENT_MAP.put("ice_element", SpellAttachments.ICE_ELEMENT);
        ELEMENT_MAP.put("lightning_element", SpellAttachments.LIGHTNING_ELEMENT);
        ELEMENT_MAP.put("water_element", SpellAttachments.WATER_ELEMENT);
        ELEMENT_MAP.put("nature_element", SpellAttachments.NATURE_ELEMENT);
    }

    public static void handleDataOnNetwork(final ElementData data, final IPayloadContext context) {
        String element = data.elementMessage;
        int entityId = data.entityIdMessage;
        int elementDuration = data.durationMessage;

        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(data.entityIdMessage);
            if (entity == null) return;

            Supplier<AttachmentType<ElementsAttachment>> attachmentSupplier = ELEMENT_MAP.get(data.elementMessage);
            if (attachmentSupplier == null) return;

            AttachmentType<ElementsAttachment> attachmentType = attachmentSupplier.get();
            if (attachmentType == null) return;

            if (data.durationMessage == 0) {
                entity.removeData(attachmentType);
            } else {
                entity.setData(attachmentType, new ElementsAttachment(data.durationMessage));
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("spellelemental.networking.failed", e.getMessage()));
            return null;
        });
    }
}