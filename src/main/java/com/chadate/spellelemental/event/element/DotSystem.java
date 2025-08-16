package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.config.ElementReactionConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通用 DOT（持续伤害）系统：根据 ReactionEffect 的 dot 参数进行周期性伤害结算。
 */
public final class DotSystem {
    private DotSystem() {}

    private static final List<DotInstance> INSTANCES = new CopyOnWriteArrayList<>();

    public static void onServerTick(ServerTickEvent.Post event) {
        if (INSTANCES.isEmpty()) return;
        INSTANCES.removeIf(DotInstance::isInvalid);
        for (DotInstance ins : INSTANCES) {
            ins.tick();
        }
        INSTANCES.removeIf(DotInstance::isFinished);
    }

    public static void registerDot(LivingEntity attacker, LivingEntity target, ElementReactionConfig.ReactionEffect effect) {
        if (target == null || !target.isAlive()) return;
        int interval = Math.max(1, effect.getIntervalTicks());
        int duration = Math.max(0, effect.getDurationTicks()); // 0 表示无限
        float tickDamage = effect.getTickDamage();
        String damageKey = effect.getDamageSource();
        String mode = effect.getDamageMode();
        float multiplier = effect.getDamageMultiplier() <= 0 ? 1.0f : effect.getDamageMultiplier();
        String baseSource = effect.getBaseSource();
        String baseAttribute = effect.getBaseAttribute();
        boolean checkEachTick = effect.isCheckRequiredEachTick();
        List<String> required = effect.getConditions() != null ? effect.getConditions().getRequiredElements() : null;

        DotInstance instance = new DotInstance(attacker, target, tickDamage, interval, duration, damageKey,
                mode, multiplier, baseSource, baseAttribute, checkEachTick, required);

        // 简单去重：避免同目标上完全相同参数的 DOT 重复注册
        for (DotInstance existing : INSTANCES) {
            if (existing.isSameKind(instance)) {
                existing.refresh(duration); // 刷新持续时间
                return;
            }
        }
        INSTANCES.add(instance);
    }

    /**
     * 注册 DOT，并显式指定必需元素集合。提供该参数时，将自动启用逐tick检查。
     */
    public static void registerDot(LivingEntity attacker, LivingEntity target, ElementReactionConfig.ReactionEffect effect,
                                   String... requiredElements) {
        if (target == null || !target.isAlive()) return;
        int interval = Math.max(1, effect.getIntervalTicks());
        int duration = Math.max(0, effect.getDurationTicks()); // 0 表示无限
        float tickDamage = effect.getTickDamage();
        String damageKey = effect.getDamageSource();
        boolean checkEachTick = true; // 指定了必需元素，强制逐tick检查
        String mode = effect.getDamageMode();
        float multiplier = effect.getDamageMultiplier() <= 0 ? 1.0f : effect.getDamageMultiplier();
        String baseSource = effect.getBaseSource();
        String baseAttribute = effect.getBaseAttribute();
        List<String> required = requiredElements == null || requiredElements.length == 0
                ? Collections.emptyList()
                : Arrays.asList(requiredElements);

        DotInstance instance = new DotInstance(attacker, target, tickDamage, interval, duration, damageKey,
                mode, multiplier, baseSource, baseAttribute, checkEachTick, required);

        for (DotInstance existing : INSTANCES) {
            if (existing.isSameKind(instance)) {
                existing.refresh(duration);
                return;
            }
        }
        INSTANCES.add(instance);
    }

    private static class DotInstance {
        @SuppressWarnings("unused")
        private final LivingEntity attacker;
        private final LivingEntity target;
        private final float tickDamage;
        private final int interval;
        private int ticksUntilNext;
        private int remainingTicks; // >0 表示剩余持续tick
        private final boolean infinite; // true 表示无限持续
        private final String damageKey;
        private final String damageMode; // 可为 null
        private final float damageMultiplier;
        private final String baseSource; // tick_damage | attribute
        private final String baseAttribute; // 当 attribute 时的属性键
        private final boolean checkEachTick;
        private final Set<String> requiredElements;

        DotInstance(LivingEntity attacker, LivingEntity target, float tickDamage, int interval, int duration, String damageKey,
                    String damageMode, float damageMultiplier, String baseSource, String baseAttribute,
                    boolean checkEachTick, List<String> requiredElements) {
            this.attacker = attacker;
            this.target = target;
            this.tickDamage = tickDamage;
            this.interval = Math.max(1, interval);
            this.ticksUntilNext = this.interval;
            this.infinite = (duration == 0);
            this.remainingTicks = Math.max(0, duration);
            this.damageKey = damageKey != null ? damageKey : "";
            this.damageMode = damageMode;
            this.damageMultiplier = damageMultiplier;
            this.baseSource = (baseSource == null || baseSource.isEmpty()) ? "tick_damage" : baseSource;
            this.baseAttribute = (baseAttribute == null || baseAttribute.isEmpty()) ? "minecraft:attack_damage" : baseAttribute;
            this.checkEachTick = checkEachTick;
            this.requiredElements = requiredElements == null ? Collections.emptySet() : new HashSet<>(requiredElements);
        }

        boolean isInvalid() {
            return target == null || !target.isAlive() || target.level().isClientSide();
        }

        boolean isFinished() {
            if (infinite) return false;
            return remainingTicks == 0;
        }

        void refresh(int duration) {
            if (duration > 0) {
                this.remainingTicks = duration;
            }
        }

        void tick() {
            if (!infinite && remainingTicks > 0) {
                remainingTicks--;
            }

            if (checkEachTick && !meetsRequiredElements()) {
                return; // 条件不满足则不结算本 tick
            }

            if (--ticksUntilNext <= 0) {
                ticksUntilNext = interval;
                applyDamage();
            }
        }

        private boolean meetsRequiredElements() {
            if (requiredElements.isEmpty()) return true;
            ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
            if (container == null) return false;
            Map<String, Integer> snap = container.snapshot();
            for (String key : requiredElements) {
                int v = snap.getOrDefault(key, 0);
                if (v <= 0) return false;
            }
            return true;
        }

        private void applyDamage() {
            DamageSource ds = resolveDamageSource(target, damageKey);
            // 计算 baseDamage：当 base_source=attribute 时使用攻击力，否则退回 tick_damage
            float baseDamage = resolveBaseDamage();
            float astral = 0f;
            try {
                if (attacker != null) {
                    astral = (float) attacker.getAttributeValue(com.chadate.spellelemental.attribute.ModAttributes.ASTRAL_BLESSING);
                }
            } catch (Exception ignored) {}

            String mode = (this.damageMode == null || this.damageMode.isEmpty()) ? "default" : this.damageMode.toLowerCase();
            float mult = this.damageMultiplier <= 0 ? 1.0f : this.damageMultiplier;
            float finalDamage;
            switch (mode) {
                case "fixed":
                    finalDamage = Math.max(0f, baseDamage); // fixed 情况使用 base 作为固定值
                    break;
                case "amplified":
                    finalDamage = baseDamage * mult * com.chadate.spellelemental.event.element.ReactionInjuryFormula.AmplifiedReactionBonus(astral);
                    break;
                case "overload":
                    finalDamage = com.chadate.spellelemental.event.element.ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
                    break;
                case "fusion":
                    // 聚变按用户要求复用超载公式
                    finalDamage = com.chadate.spellelemental.event.element.ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
                    break;
                default:
                    finalDamage = baseDamage * mult;
                    break;
            }
            // 输出 DOT 伤害日志，包含伤害源
            try {
                String msgId = ds.getMsgId();
                SpellElemental.LOGGER.info("DOT tick -> target={}, damage={}, sourceKey={}, damageTypeMsgId={}",
                        target.getName().getString(), finalDamage, this.damageKey, msgId);
            } catch (Exception ignored) {}
            target.hurt(ds, finalDamage);
        }

        private float resolveBaseDamage() {
            if ("attribute".equalsIgnoreCase(this.baseSource)) {
                if (attacker != null) {
                    try {
                        // 仅支持 minecraft:attack_damage
                        if ("minecraft:attack_damage".equalsIgnoreCase(this.baseAttribute)) {
                            return (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                        }
                    } catch (Exception ignored) {}
                }
                return 0f;
            }
            // 默认回退为 tick_damage
            return this.tickDamage;
        }

        private static DamageSource resolveDamageSource(LivingEntity victim, String key) {
            if (key == null || key.isEmpty()) return victim.damageSources().magic();

            // 支持命名空间 DamageType，例如 "irons_spellbooks:fire_magic" 或 "minecraft:in_fire"
            if (key.contains(":")) {
                try {
                    ResourceLocation id = ResourceLocation.tryParse(key);
                    if (id != null) {
                        var reg = victim.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                        ResourceKey<DamageType> rkey = ResourceKey.create(Registries.DAMAGE_TYPE, id);
                        var holderOpt = reg.getHolder(rkey);
                        if (holderOpt.isPresent()) {
                            return victim.damageSources().source(rkey);
                        }
                    }
                } catch (Exception ignored) {}
                // 命名空间解析失败则继续走下方的简写匹配
            }

            // 仅支持命名空间注册键，未解析成功则回退为 magic
            return victim.damageSources().magic();
        }

        boolean isSameKind(DotInstance other) {
            return other != null && other.target == this.target && other.tickDamage == this.tickDamage
                    && other.interval == this.interval && Objects.equals(other.damageKey, this.damageKey)
                    && Objects.equals(other.requiredElements, this.requiredElements)
                    && Objects.equals(other.damageMode, this.damageMode)
                    && other.damageMultiplier == this.damageMultiplier
                    && Objects.equals(other.baseSource, this.baseSource)
                    && Objects.equals(other.baseAttribute, this.baseAttribute);
        }
    }
}
