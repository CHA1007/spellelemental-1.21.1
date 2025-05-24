package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.damage.OverloadReactionHandler;
import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public class ReactionEvent {
    public static void MagicAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB area = target.getBoundingBox().inflate(box);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        float overloadDamage = ReactionInjuryFormula.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        for (LivingEntity entity : entities) {
            if (entity.isAlive() && !entity.isSpectator()) {
                DamageSource damageSource = attacker.damageSources().magic();
                entity.hurt(damageSource, overloadDamage);
            }
        }
    }

    public static void LightningAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB area = target.getBoundingBox().inflate(box);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        float overloadDamage = ReactionInjuryFormula.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        for (LivingEntity entity : entities) {
            if (entity.isAlive() && !entity.isSpectator()) {
                DamageSource damageSource = attacker.damageSources().lightningBolt();
                entity.hurt(damageSource, overloadDamage);
            }
        }
    }

    public static void FreezeAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB area = target.getBoundingBox().inflate(box);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        float overloadDamage = ReactionInjuryFormula.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        for (LivingEntity entity : entities) {
            if (entity.isAlive() && !entity.isSpectator()) {
                DamageSource damageSource = attacker.damageSources().freeze();
                entity.hurt(damageSource, overloadDamage);
            }
        }
    }

    public static void FireAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB area = target.getBoundingBox().inflate(box);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        float overloadDamage = ReactionInjuryFormula.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        for (LivingEntity entity : entities) {
            if (entity.isAlive() && !entity.isSpectator()) {
                DamageSource damageSource = attacker.damageSources().inFire();
                entity.hurt(damageSource, overloadDamage);
            }
        }
    }

  public static void ConsumeElement(LivingDamageEvent.Pre event, String consumedElement, int consumeAmount, String resetElement) {
    LivingEntity target = event.getEntity();
    int current = getElementAttachment(target, consumedElement);
    int newAmount = Math.max(current - consumeAmount, 0);

    setElementAttachment(target, consumedElement, newAmount);
    setElementAttachment(target, resetElement, 0);
}

    public static void setElementAttachment(LivingEntity entity, String element, int value) {
        switch (element.toLowerCase()) {
            case "fire" -> entity.getData(SpellAttachments.FIRE_ELEMENT).setValue(value);
            case "ice" -> entity.getData(SpellAttachments.ICE_ELEMENT).setValue(value);
            case "lightning" -> entity.getData(SpellAttachments.LIGHTNING_ELEMENT).setValue(value);
            case "water" -> entity.getData(SpellAttachments.WATER_ELEMENT).setValue(value);
            case "nature" -> entity.getData(SpellAttachments.NATURE_ELEMENT).setValue(value);
            case "electro" -> entity.getData(SpellAttachments.PROMOTION_ELEMENT).setValue(value);
            case "freeze" -> entity.getData(SpellAttachments.FREEZE_ELEMENT).setValue(value);
            case "dewspark" -> entity.getData(SpellAttachments.DEWSPARK_LAYERS).setValue(value);
            case "blood" -> entity.getData(SpellAttachments.BLOOD_ELEMENT).setValue(value);
            default -> throw new IllegalArgumentException("未知元素: " + element);
        }
    }

    public static int getElementAttachment(LivingEntity entity, String element) {
        return switch (element.toLowerCase()) {
            case "fire" -> entity.getData(SpellAttachments.FIRE_ELEMENT).getValue();
            case "ice" -> entity.getData(SpellAttachments.ICE_ELEMENT).getValue();
            case "lightning" -> entity.getData(SpellAttachments.LIGHTNING_ELEMENT).getValue();
            case "water" -> entity.getData(SpellAttachments.WATER_ELEMENT).getValue();
            case "nature" -> entity.getData(SpellAttachments.NATURE_ELEMENT).getValue();
            case "electro" -> entity.getData(SpellAttachments.PROMOTION_ELEMENT).getValue();
            case "freeze" -> entity.getData(SpellAttachments.FREEZE_ELEMENT).getValue();
            case "dewspark" -> entity.getData(SpellAttachments.DEWSPARK_LAYERS).getValue();
            case "blood" -> entity.getData(SpellAttachments.BLOOD_ELEMENT).getValue();
            default -> throw new IllegalArgumentException("未知元素: " + element);
        };
    }
}
