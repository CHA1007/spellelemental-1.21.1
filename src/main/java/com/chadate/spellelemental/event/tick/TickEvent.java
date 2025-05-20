package com.chadate.spellelemental.event.tick;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.event.element.DamageEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

public class TickEvent {
    public static void CheckDewSparkLayer(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int time = getDewSparkTimetValue(target);
        int layers = getDewSparkLayersValue(target);
        if (time > 0) {
            time -= 1;
            setDewSparkTimeValue(target, time);

            if (layers > 10) {
                applyPoisonDamage(target, 1);
                setDewSparkLayersValue(target, layers - 1);
            }

            if (time <= 0) {
                if (layers > 0 ) {
                    applyPoisonDamage(target, layers);
                    clearElementData(target);
                }
            }
        }
    }

    private static int getDewSparkTimetValue(LivingEntity target) {
        return target.getData(SpellAttachments.DEWSPARK_TIME).getValue();
    }

    private static int getDewSparkLayersValue(LivingEntity target) {
        return target.getData(SpellAttachments.DEWSPARK_LAYERS).getValue();
    }
    private static void setDewSparkTimeValue(LivingEntity target, int value) {
        target.getData(SpellAttachments.DEWSPARK_TIME.get()).setValue(value);
    }
    private static void setDewSparkLayersValue(LivingEntity target, int value) {
        target.getData(SpellAttachments.DEWSPARK_LAYERS.get()).setValue(value);
    }

    private static void clearElementData(LivingEntity target) {
        target.removeData(SpellAttachments.DEWSPARK_TIME.get());
        target.removeData(SpellAttachments.DEWSPARK_LAYERS.get());
    }

    private static void applyPoisonDamage(LivingEntity target, int layers) {
        Level level = target.level();
        AABB aabb = target.getBoundingBox().inflate(3.0D);

        List<LivingEntity> targets = level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                aabb,
                e ->  e.isAlive() && !(e instanceof Player)
        );

        float basePoisonDamage = target.getData(SpellAttachments.DEWSPARK_DAMAGE).getValue();
        DamageSource damageSource = target.damageSources().magic();

        for (LivingEntity victim : targets) {
            int finalDamage = (int)(basePoisonDamage * layers);
            victim.hurt(damageSource, finalDamage);

        }
    }
    public static void FreezeResistanceDecay(EntityTickEvent.Pre event) {
        if(event.getEntity().tickCount % 200 != 0)return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int freezeLayers = target.getData(SpellAttachments.FREEZE_LAYERS).getValue();

        if (freezeLayers > 0 ){
            int newFreezeLayers = Math.max( freezeLayers - 1, 0);
            target.getData(SpellAttachments.FREEZE_LAYERS).setValue(newFreezeLayers);
        }else if (freezeLayers == 0){
            target.removeData(SpellAttachments.FREEZE_ELEMENT);
        }
    }


    public static void VulnerabilityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        ElementsAttachment vulnerabilityData = livingEntity.getData(SpellAttachments.VULNERABILITY_ELEMENT);
        int duration = vulnerabilityData.getValue();

        if (duration > 0) {
            livingEntity.getData(SpellAttachments.VULNERABILITY_ELEMENT).setValue(Math.max(duration - 1, 0));
        }

        if (duration <= 0) {
            AttributeInstance resistAttribute = livingEntity.getAttribute(ModAttributes.PHYSICAL_DAMAGE_RESIST);
            if (resistAttribute != null) {
                resistAttribute.removeModifier(ResourceLocation.parse("vulnerable"));
            }

            livingEntity.getData(SpellAttachments.VULNERABILITY_ELEMENT).setValue(0);
            livingEntity.removeData(SpellAttachments.VULNERABILITY_ELEMENT);
        }
    }
}
