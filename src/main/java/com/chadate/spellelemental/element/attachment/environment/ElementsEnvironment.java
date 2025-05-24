package com.chadate.spellelemental.element.attachment.environment;

import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.entity.LivingEntity;

public class ElementsEnvironment {
    public static void applyWaterElementAttachment(LivingEntity entity){
        if (entity.isInWaterOrRain()) {
            entity.getData(SpellAttachments.WATER_ELEMENT).setValue(200);
            if (entity.getData(SpellAttachments.FIRE_ELEMENT).getValue() > 0){
                entity.getData(SpellAttachments.FIRE_ELEMENT).setValue(0);
            }
        }
    }
}
