package com.chadate.spellelemental.event.custom;

import com.chadate.spellelemental.attribute.ModAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Objects;

public class PhysicalDamageEvent {
    public static void PhysicalDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
        double originalDamage = event.getNewDamage();
        double physicalBoost = 1;
        DamageSource source = event.getSource();

        if (isPhysicalDamage(source)) {
            if (attacker != null) {
                physicalBoost = Objects.requireNonNull(attacker.getAttribute(ModAttributes.PHYSICAL_DAMAGE_BOOST)).getValue();
            }
            double physicalResist = Objects.requireNonNull(target.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST)).getValue();
            float finalDamage = (float) (originalDamage * physicalBoost * PhysicalResistMultiplier(physicalResist));
            event.setNewDamage(finalDamage);
        }
    }

    public static double PhysicalResistMultiplier(double physicalResist){
        double resistanceMultiplier;
        if (physicalResist - 1 < 0) {
            resistanceMultiplier = 1 - ((physicalResist - 1 ) / 2);
        } else if (physicalResist - 1 < 0.75) {
            resistanceMultiplier = 1 - (physicalResist - 1 );
        } else {
            resistanceMultiplier = 1 / (1 + 4 * (physicalResist - 1 ));
        }
        return resistanceMultiplier;
    }

    public static boolean isPhysicalDamage(DamageSource source) {
        return source.is(Tags.DamageTypes.IS_PHYSICAL);
    }
}
