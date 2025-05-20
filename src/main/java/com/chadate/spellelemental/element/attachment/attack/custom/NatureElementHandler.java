package com.chadate.spellelemental.element.attachment.attack.custom;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.BaseElementHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class NatureElementHandler extends BaseElementHandler {
    public NatureElementHandler() {
        super("nature", SpellAttachments.NATURE_ELEMENT);
    }

    @Override
    public void applyEffect(LivingEntity target, DamageSource source, int entityId) {
        super.applyEffect(target, source, entityId);
    }
}