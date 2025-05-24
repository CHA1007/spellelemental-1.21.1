package com.chadate.spellelemental.cilent.network.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.ElementsAttachment;
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
        ELEMENT_MAP.put("ender_element", SpellAttachments.ENDER_ELEMENT);
        ELEMENT_MAP.put("blood_element", SpellAttachments.BLOOD_ELEMENT);
        ELEMENT_MAP.put("holy_element", SpellAttachments.HOLY_ELEMENT);
        ELEMENT_MAP.put("freeze_element", SpellAttachments.FREEZE_ELEMENT);
        ELEMENT_MAP.put("promotion_element", SpellAttachments.PROMOTION_ELEMENT);
    }
    public static void handleDataOnNetwork(final ElementData data, final IPayloadContext context) {
        String element = data.elementMessage;
        int entityId = data.entityIdMessage;
        int elementDuration = data.durationMessage;

        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(entityId);
            if (entity == null) return;

            Supplier<AttachmentType<ElementsAttachment>> attachmentSupplier = ELEMENT_MAP.get(element);
            if (attachmentSupplier == null) return;

            AttachmentType<ElementsAttachment> attachmentType = attachmentSupplier.get();
            if (attachmentType == null) return;

            if (data.durationMessage == 0) {
                entity.removeData(attachmentType);
            } else {
                entity.setData(attachmentType, new ElementsAttachment(elementDuration));
            }
        });
    }
}