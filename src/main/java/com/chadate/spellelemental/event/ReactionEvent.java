package com.chadate.spellelemental.event;

import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

public class ReactionEvent {
    public static void MagicAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB aabb = target.getBoundingBox().inflate(box);

        List<LivingEntity> targets = level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                aabb,
                entity -> entity != attacker && entity.isAlive() && !entity.isSpectator()
        );

        float overloadDamage = ReactionEvent.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        DamageSource damageSource = attacker.damageSources().magic();
        for (LivingEntity entity : targets) {
            entity.hurt(damageSource, overloadDamage);
            DamageEvent.CancelSpellUnbeatableFrames(entity);
        }
    }

    public static void LightningAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB aabb = target.getBoundingBox().inflate(box);

        List<LivingEntity> targets = level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                aabb,
                entity -> entity != attacker && entity.isAlive() && !entity.isSpectator()
        );

        float overloadDamage = ReactionEvent.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        DamageSource damageSource = attacker.damageSources().lightningBolt();
        for (LivingEntity entity : targets) {
            entity.hurt(damageSource, overloadDamage);
            DamageEvent.CancelSpellUnbeatableFrames(entity);
        }
    }

    public static void FreezeAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB aabb = target.getBoundingBox().inflate(box);

        List<LivingEntity> targets = level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                aabb,
                entity -> entity != attacker && entity.isAlive() && !entity.isSpectator()
        );

        float overloadDamage = ReactionEvent.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        DamageSource damageSource = attacker.damageSources().freeze();
        for (LivingEntity entity : targets) {
            entity.hurt(damageSource, overloadDamage);
            DamageEvent.CancelSpellUnbeatableFrames(entity);
        }
    }

    public static void FireAreaDamage(LivingEntity target, float box, LivingEntity attacker, double attackDamage, float multiplier, float astralBlessing){
        Level level = target.level();
        AABB aabb = target.getBoundingBox().inflate(box);

        List<LivingEntity> targets = level.getEntities(
                EntityTypeTest.forClass(LivingEntity.class),
                aabb,
                entity -> entity != attacker && entity.isAlive() && !entity.isSpectator()
        );

        float overloadDamage = ReactionEvent.CalculateOverloadDamage((float) attackDamage, multiplier, astralBlessing);

        DamageSource damageSource = attacker.damageSources().inFire();
        for (LivingEntity entity : targets) {
            entity.hurt(damageSource, overloadDamage);
            DamageEvent.CancelSpellUnbeatableFrames(entity);
        }
    }

    public static float ElectroReactiveteDamage(float originalDamage, float reactivityMultiplier, float astralBlessing) {
        return originalDamage * reactivityMultiplier * (1 + (5 * astralBlessing) / (astralBlessing + 1200));
    }

  public static void ConsumeElement(LivingDamageEvent.Pre event, String consumedElement, int consumeAmount, String resetElement) {
    LivingEntity target = event.getEntity();
    int current = getElementAttachment(target, consumedElement);
    int newAmount = Math.max(current - consumeAmount, 0);

    setElementAttachment(target, consumedElement, newAmount);
    setElementAttachment(target, resetElement, 0);
}

    public static float CalculateOverloadDamage(float attackDamage, float reactionMultiplier, float astralBlessing) {
        return attackDamage * reactionMultiplier * (1 + (16 * astralBlessing) / (astralBlessing + 2000));
    }

    public static float CalculateBlessingBonus(float astralBlessing) {
        return (float) (1 + ((2.78 * astralBlessing) / (astralBlessing + 1400)));
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
            default -> throw new IllegalArgumentException("未知元素: " + element);
        };
    }
}
