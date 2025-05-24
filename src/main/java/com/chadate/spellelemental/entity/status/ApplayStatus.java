package com.chadate.spellelemental.entity.status;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class ApplayStatus {

    public static void applyVulnerability(LivingDamageEvent.Pre event, float amount, AttributeModifier.Operation operation, int duration) {
        LivingEntity target = event.getEntity();
        boolean hasVulnerable = Objects.requireNonNull(target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST))
                .hasModifier(ResourceLocation.parse("vulnerable"));

            if (!hasVulnerable)

        {
            Objects.requireNonNull(target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST)).addTransientModifier(
                    new AttributeModifier(ResourceLocation.parse("vulnerable"), amount, operation));
            event.getEntity().getData(SpellAttachments.VULNERABILITY_ELEMENT).setValue(duration);
        }
    }

    public static void applyToReduceTheTherapeuticEffect(LivingDamageEvent.Pre event, float amount, AttributeModifier.Operation operation, int duration) {
        LivingEntity target = event.getEntity();
        boolean hasVulnerable = Objects.requireNonNull(target.getAttribute(ModAttributes.HEALING_POWER))
                .hasModifier(ResourceLocation.parse("boiling_blood"));

        if (!hasVulnerable)

        {
            Objects.requireNonNull(target.getAttribute(ModAttributes.HEALING_POWER)).addTransientModifier(
                    new AttributeModifier(ResourceLocation.parse("boiling_blood"), amount, operation));
            event.getEntity().getData(SpellAttachments.BOLIING_BLOOD).setValue(duration);
        }
    }
}
