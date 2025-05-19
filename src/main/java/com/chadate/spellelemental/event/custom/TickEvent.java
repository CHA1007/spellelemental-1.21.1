package com.chadate.spellelemental.event.custom;

import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.custom.ElementsAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
            time -= 10;
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
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int freezeLayers = target.getData(SpellAttachments.FREEZE_LAYERS).getValue();

        if (freezeLayers > 0 ){
            int newFreezeLayers = Math.max( freezeLayers - 1, 0);
            target.getData(SpellAttachments.FREEZE_LAYERS).setValue(newFreezeLayers);
        }else if (freezeLayers == 0){
            target.removeData(SpellAttachments.FREEZE_ELEMENT);
        }
    }

    public static void FreezeElementTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int freezeDuration = target.getData(SpellAttachments.FREEZE_ELEMENT).getValue();
        int newFreezeDuration = Math.max( freezeDuration - 40, 0);
        target.getData(SpellAttachments.FREEZE_ELEMENT).setValue(newFreezeDuration);

        if (newFreezeDuration == 0) {
            target.removeData(SpellAttachments.FREEZE_ELEMENT);

            if (target instanceof Player player){
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }else{

                if (target instanceof Mob mob) {
                    mob.setNoAi(false);
                }
            }
        }
    }

    public static void CheckFreezeStatus(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (target.getData(SpellAttachments.ICE_ELEMENT).getValue() > 0 && target.getData(SpellAttachments.WATER_ELEMENT).getValue() > 0 && !(target.getData(SpellAttachments.FREEZE_ELEMENT).getValue() > 0)){

            if (target instanceof Player player) {
                int freezeResistanceLayers = target.getData(SpellAttachments.FREEZE_LAYERS).getValue();
                int freezeDuration = target.getData(SpellAttachments.WATER_ELEMENT).getValue()
                        + target.getData(SpellAttachments.ICE_ELEMENT).getValue();
                target.getData(SpellAttachments.FREEZE_ELEMENT).setValue((int) ( freezeDuration * (1 - freezeResistanceLayers * 0.1)));
                target.getData(SpellAttachments.WATER_ELEMENT).setValue(0);
                target.getData(SpellAttachments.ICE_ELEMENT).setValue(0);

                if (freezeResistanceLayers > 0) {
                    int newFreezeResistanceLayers =Math.min( freezeResistanceLayers + 1, 5);
                    target.getData(SpellAttachments.FREEZE_LAYERS).setValue(newFreezeResistanceLayers);
                }else {
                    target.getData(SpellAttachments.FREEZE_LAYERS).setValue(1);
                }

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, freezeDuration, 255, false, false, false));
            }else {
                int freezeResistanceLayers = target.getData(SpellAttachments.FREEZE_LAYERS).getValue();
                int freezeDuration = target.getData(SpellAttachments.WATER_ELEMENT).getValue()
                        + target.getData(SpellAttachments.ICE_ELEMENT).getValue();
                target.getData(SpellAttachments.FREEZE_ELEMENT).setValue((int) ( freezeDuration * (1 - freezeResistanceLayers * 0.15)));
                target.getData(SpellAttachments.WATER_ELEMENT).setValue(0);
                target.getData(SpellAttachments.ICE_ELEMENT).setValue(0);
                if (freezeResistanceLayers > 0) {
                    int newFreezeResistanceLayers =Math.min( freezeResistanceLayers + 1, 5);
                    target.getData(SpellAttachments.FREEZE_LAYERS).setValue(newFreezeResistanceLayers);
                }else {
                    target.getData(SpellAttachments.FREEZE_LAYERS).setValue(1);
                }
                if (target instanceof Mob mob) {
                    mob.setNoAi(true);
                }
            }
        }

    }

    public static void VulnerabilityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        ElementsAttachment vulnerabilityData = livingEntity.getData(SpellAttachments.VULNERABILITY_ELEMENT);
        int duration = vulnerabilityData.getValue();

        if (duration > 0) {
            livingEntity.getData(SpellAttachments.VULNERABILITY_ELEMENT).setValue(Math.max(duration - 10, 0));
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

    public static void BurnTick(EntityTickEvent.Pre event){
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (target.getData(SpellAttachments.FIRE_ELEMENT).getValue() != 0
                && target.getData(SpellAttachments.NATURE_ELEMENT).getValue() != 0
                 && target.getData(SpellAttachments.WATER_ELEMENT).getValue() == 0 ) {

            target.hurt(event.getEntity().level().damageSources().inFire(), event.getEntity().getData(SpellAttachments.BURN_DAMAGE).getValue());
            target.getData(SpellAttachments.FIRE_ELEMENT).setValue(200);
        }
    }

    public static void ElectroReaction(EntityTickEvent.Pre event){
        if (event.getEntity().getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() != 0 && event.getEntity().getData(SpellAttachments.WATER_ELEMENT).getValue() != 0){
            event.getEntity().getData(SpellAttachments.LIGHTNING_ELEMENT).setValue(event.getEntity().getData(SpellAttachments.LIGHTNING_ELEMENT).getValue() - 80);
            event.getEntity().getData(SpellAttachments.WATER_ELEMENT).setValue(event.getEntity().getData(SpellAttachments.WATER_ELEMENT).getValue() - 80);
            if (event.getEntity().getData(SpellAttachments.ELECTRO_DAMAGE).getValue() != 0){
                event.getEntity().hurt(event.getEntity().level().damageSources().lightningBolt(), event.getEntity().getData(SpellAttachments.ELECTRO_DAMAGE).getValue());
            }else {
                event.getEntity().hurt(event.getEntity().level().damageSources().lightningBolt(), 5);
            }
        }
    }
}
