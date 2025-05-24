package com.chadate.spellelemental.element.attachment.attack.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.BaseElementHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class BloodElementHandler extends BaseElementHandler {
    public BloodElementHandler() {
        super("blood", SpellAttachments.BLOOD_ELEMENT);
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        super.applyEffect(target, source, entityId);
    }
}
