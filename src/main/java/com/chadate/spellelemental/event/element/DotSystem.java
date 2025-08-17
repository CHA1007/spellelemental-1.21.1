package com.chadate.spellelemental.event.element;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.config.ElementReactionConfig;
import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.util.DamageAttachmentGuards;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 通用 DOT（持续伤害）系统：
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
                mode, multiplier, baseSource, baseAttribute, checkEachTick, required,
                effect.isCheckRequiredEachTick(), // 保持原有布尔传递
                effect.isDotAttachEnabled(), effect.getDotAttachElement(),
                Math.max(0, effect.getDotAttachAmount()), effect.getDotAttachMax());

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
                mode, multiplier, baseSource, baseAttribute, checkEachTick, required,
                checkEachTick,
                effect.isDotAttachEnabled(), effect.getDotAttachElement(),
                Math.max(0, effect.getDotAttachAmount()), effect.getDotAttachMax());

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
        // DOT 附着配置
        private final boolean dotAttachEnabled;
        private final String dotAttachElement;
        private final int dotAttachAmount;
        private final int dotAttachMax;
        private final Set<String> requiredElements;

        DotInstance(LivingEntity attacker, LivingEntity target, float tickDamage, int interval, int duration, String damageKey,
                    String damageMode, float damageMultiplier, String baseSource, String baseAttribute,
                    boolean checkEachTick, List<String> requiredElements,
                    boolean dummyKeepCompat,
                    boolean dotAttachEnabled, String dotAttachElement, int dotAttachAmount, int dotAttachMax) {
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
            this.dotAttachEnabled = dotAttachEnabled;
            this.dotAttachElement = dotAttachElement;
            this.dotAttachAmount = Math.max(0, dotAttachAmount);
            this.dotAttachMax = dotAttachMax;
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

            float finalDamage = getFinalDamage(baseDamage, astral);
            // 输出 DOT 伤害日志，包含伤害源
            try {
                String msgId = ds.getMsgId();
                SpellElemental.LOGGER.info("DOT tick -> target={}, damage={}, sourceKey={}, damageTypeMsgId={}",
                        target.getName().getString(), finalDamage, this.damageKey, msgId);
            } catch (Exception ignored) {}
            DamageAttachmentGuards.runAsNonAttachable(() -> target.hurt(ds, finalDamage));

            // 按配置附着元素到目标，并同步客户端
            if (dotAttachEnabled && dotAttachAmount > 0 && dotAttachElement != null && !dotAttachElement.isEmpty()) {
                try {
                    ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
                    var snap = container.snapshot();
                    int cur = snap.getOrDefault(dotAttachElement, 0);
                    int capped = (dotAttachMax > 0) ? Math.min(cur + dotAttachAmount, dotAttachMax) : cur + dotAttachAmount;
                    if (capped != cur) {
                        container.setValue(dotAttachElement, capped);
                        // 广播单键更新（沿用 ElementData 的单值消息语义：第三参数作为数值）
                        PacketDistributor.sendToAllPlayers(new ElementData(target.getId(), dotAttachElement, capped));
                    }
                } catch (Exception ignored) {}
            }
        }

        private float getFinalDamage(float baseDamage, float astral) {
            String mode = (this.damageMode == null || this.damageMode.isEmpty()) ? "default" : this.damageMode.toLowerCase();
            float mult = this.damageMultiplier <= 0 ? 1.0f : this.damageMultiplier;
            return switch (mode) {
                case "fixed" -> Math.max(0f, baseDamage); // fixed 情况使用 base 作为固定值
                case "amplified" -> baseDamage * mult * ReactionInjuryFormula.AmplifiedReactionBonus(astral);
                case "fusion" -> ReactionInjuryFormula.CalculateOverloadDamage(baseDamage, mult, astral);
                default -> baseDamage * mult;
            };
        }

        private float resolveBaseDamage() {
            if ("attribute".equalsIgnoreCase(this.baseSource)) {
                if (attacker != null) {
                    try {
                        // 支持命名空间 Attribute
                        String keyStr = (this.baseAttribute == null || this.baseAttribute.isEmpty())
                                ? "minecraft:attack_damage"
                                : this.baseAttribute;

                        // 允许简写（无命名空间时默认 minecraft）
                        ResourceLocation id = keyStr.contains(":")
                                ? ResourceLocation.tryParse(keyStr)
                                : ResourceLocation.fromNamespaceAndPath("minecraft", keyStr);

                        if (id != null) {
                            var reg = attacker.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE);
                            ResourceKey<net.minecraft.world.entity.ai.attributes.Attribute> rkey =
                                    ResourceKey.create(Registries.ATTRIBUTE, id);
                            var holderOpt = reg.getHolder(rkey);
                            if (holderOpt.isPresent()) {
                                return (float) attacker.getAttributeValue(holderOpt.get());
                            }
                        }
                        // 如果解析失败则退回到原版攻击力
                        return (float) attacker.getAttributeValue(
                                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
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
                    && Objects.equals(other.baseAttribute, this.baseAttribute)
                    && this.dotAttachEnabled == other.dotAttachEnabled
                    && Objects.equals(this.dotAttachElement, other.dotAttachElement)
                    && this.dotAttachAmount == other.dotAttachAmount
                    && this.dotAttachMax == other.dotAttachMax;
        }
    }
}
