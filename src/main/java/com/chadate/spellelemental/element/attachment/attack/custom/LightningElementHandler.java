package com.chadate.spellelemental.element.attachment.attack.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.BaseElementHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LightningElementHandler extends BaseElementHandler {
    public LightningElementHandler() {
        super("lightning", SpellAttachments.LIGHTNING_ELEMENT);
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        super.applyEffect(target, source, entityId);
    }
}