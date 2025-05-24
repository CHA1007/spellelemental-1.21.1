package com.chadate.spellelemental.element.attachment.attack;

import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.cilent.network.custom.ElementData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Supplier;

public abstract class BaseElementHandler implements ElementAttachmentHandler {
    private final String elementName;
    private final Supplier<AttachmentType<ElementsAttachment>> attachmentSupplier;

    public BaseElementHandler(String elementName,
                              Supplier<AttachmentType<ElementsAttachment>> attachmentSupplier) {
        this.elementName = elementName;
        this.attachmentSupplier = attachmentSupplier;
    }

    @Override
    public boolean canApply(LivingEntity target, DamageSource source) {
        return source.getMsgId().equals(elementName + "_magic");
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        AttachmentType<ElementsAttachment> type = attachmentSupplier.get();
        target.getData(type).setValue(200);
        PacketDistributor.sendToAllPlayers(new ElementData(entityId, elementName + "_element", 200));
    }
}
