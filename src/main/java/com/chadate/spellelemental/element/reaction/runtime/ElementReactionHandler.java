package com.chadate.spellelemental.element.reaction.runtime;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.attribute.ModAttributes;
import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.data.ElementReactionRegistry;
import com.chadate.spellelemental.event.element.ReactionInjuryFormula;
import com.chadate.spellelemental.util.DamageAttachmentGuards;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 元素反应处理器
 * 负责检测、触发和执行元素反应
 */
public class ElementReactionHandler {
    // 当为 true 时，跳过元素附着/反应计算（用于本类内部触发的 AOE 等“二次伤害”）
    public static final ThreadLocal<Boolean> SUPPRESS_ATTACHMENT = ThreadLocal.withInitial(() -> false);
    private static final int DEFAULT_CONSUME_PER_SIDE = 1; // 仅作为兜底，优先读取注册表中的方向化消耗
    
    /**
     * 伤害类型反应
     */
    public static void damageTypeReaction(LivingDamageEvent.Pre event) {
        // 若被 AOE 标记抑制，则不进行反应与附着
        if (Boolean.TRUE.equals(SUPPRESS_ATTACHMENT.get())) {
            return;
        }
        // 若无任何 damage 类型的反应或未建立任何组合索引，直接返回
        if (ElementReactionRegistry.getDamageReactions().isEmpty()) return;
        if (!ElementReactionRegistry.hasAnyDamageCombos()) return;
        LivingEntity victim = event.getEntity();
        if (victim == null) return;
        DamageSource dmgSource = event.getSource();
        Entity direct = dmgSource.getDirectEntity();
        Entity attacker = dmgSource.getEntity();

        // 收集候选元素：来源(攻击方)与目标(受击方)
        List<String> targetCandidates = collectPresentElements(victim);
        List<String> sourceCandidates = new ArrayList<>();
        if (attacker instanceof LivingEntity livingAttacker) {
            sourceCandidates = collectPresentElements(livingAttacker);
        } else if (direct instanceof LivingEntity livingDirect) {
            sourceCandidates = collectPresentElements(livingDirect);
        }

        // 当攻击方没有任何元素时，尝试在同一目标实体上进行元素对匹配（如先冰后火都附着在受击者身上）
        boolean intraTargetMode = false;
        if (sourceCandidates.isEmpty() && !targetCandidates.isEmpty()) {
            intraTargetMode = true;
        }
        if (targetCandidates.isEmpty() && sourceCandidates.isEmpty()) return;

        // 基于注册表进行最小匹配：找到首个(source, target)满足反应索引的组合
        String matchedSource = null;
        String matchedTarget = null;
        String reactionId = null;
        if (!intraTargetMode) {
            outer:
            for (String s : sourceCandidates) {
                for (String t : targetCandidates) {
                    String rid = ElementReactionRegistry.findDamageReactionId(s, t);
                    if (rid != null) {
                        matchedSource = s;
                        matchedTarget = t;
                        reactionId = rid;
                        break outer;
                    }
                }
            }
        } else {
            // 在同一实体上寻找两个不同元素的有效组合。
            // 1) 首选：最近附着的元素（lastApplied）作为 source。
            String inferredSource = null;
            if (victim != null) {
                ElementContainerAttachment vc = victim.getData(SpellAttachments.ELEMENTS_CONTAINER);
                long bestTime = Long.MIN_VALUE;
                for (String c : targetCandidates) {
                    long t = vc.getLastApplied(c);
                    if (t > bestTime) {
                        bestTime = t;
                        inferredSource = c;
                    }
                }
                // 若没有时间戳（bestTime 仍为最小），视为未知
                if (bestTime == Long.MIN_VALUE) inferredSource = null;
            }

            // 2) 次选：基于 DamageSource 的 msgId 推断。
            if (inferredSource == null) {
                inferredSource = inferSourceFromDamage(dmgSource, targetCandidates);
            }

            if (inferredSource != null) {
                // 先尝试以推断/最近附着的来源为 source 的方向
                for (String t : targetCandidates) {
                    if (t.equalsIgnoreCase(inferredSource)) continue;
                    String rid = ElementReactionRegistry.findDamageReactionId(inferredSource, t);
                    if (rid != null) {
                        matchedSource = inferredSource;
                        matchedTarget = t;
                        reactionId = rid;
                        break;
                    }
                }
            }
            if (reactionId == null) {
                // 回退到原来的双重循环查找任意有效方向
                outer2:
                for (int i = 0; i < targetCandidates.size(); i++) {
                    for (int j = 0; j < targetCandidates.size(); j++) {
                        if (i == j) continue; // 需要两个不同元素
                        String s = targetCandidates.get(i);
                        String t = targetCandidates.get(j);
                        String rid = ElementReactionRegistry.findDamageReactionId(s, t);
                        if (rid != null) {
                            matchedSource = s;
                            matchedTarget = t;
                            reactionId = rid;
                            break outer2;
                        }
                    }
                }
            }
        }

        if (reactionId == null) return;

        // 命中某个反应ID：执行元素量消耗（优先使用注册表中的定制消耗，其余效果保持 TODO）
        int[] consume = ElementReactionRegistry.getDamageConsumeFor(matchedSource, matchedTarget);
        int consumeSource = consume.length > 0 ? consume[0] : DEFAULT_CONSUME_PER_SIDE;
        int consumeTarget = consume.length > 1 ? consume[1] : DEFAULT_CONSUME_PER_SIDE;

        // 记录消耗前的剩余值（若可用）
        int beforeVictimTarget = getElementValue(victim, matchedTarget);
        int beforeVictimSource = getElementValue(victim, matchedSource);
        int beforeAttackerSource = (attacker instanceof LivingEntity la) ? getElementValue(la, matchedSource) : -1;
        int beforeDirectSource = (direct instanceof LivingEntity ld) ? getElementValue(ld, matchedSource) : -1;

        consumeElementOnEntity(victim, matchedTarget, Math.max(0, consumeTarget));

        // 尝试扣减施加方来源元素量（若为生物且其容器中存在该元素）
        boolean consumedSource = false;
        if (!intraTargetMode) {
            if (attacker instanceof LivingEntity livingAttacker) {
                consumeElementOnEntity(livingAttacker, matchedSource, Math.max(0, consumeSource));
                consumedSource = true;
            } else if (direct instanceof LivingEntity livingDirect) {
                // 若无 attacker，但有直接实体为生物，则尝试在其上扣减
                consumeElementOnEntity(livingDirect, matchedSource, Math.max(0, consumeSource));
                consumedSource = true;
            }
        }
        // 若未能在攻击方扣减（或是同体反应模式），则从受击者身上扣减来源元素
        if (!consumedSource) {
            consumeElementOnEntity(victim, matchedSource, Math.max(0, consumeSource));
        }

        // 记录消耗后的剩余值（若可用）
        int afterVictimTarget = getElementValue(victim, matchedTarget);
        int afterVictimSource = getElementValue(victim, matchedSource);
        int afterAttackerSource = (attacker instanceof LivingEntity la2) ? getElementValue(la2, matchedSource) : -1;
        int afterDirectSource = (direct instanceof LivingEntity ld2) ? getElementValue(ld2, matchedSource) : -1;

        // 详细调试输出（含消耗前/后剩余值）
        SpellElemental.LOGGER.info(
                "ElementReaction triggered: id={}, mode={}, src={}, tgt={}, consume=[{},{}], victim={}, attacker={}, direct={}, inferredSource={}, before(victim:src->{};tgt->{}; attacker:src->{}; direct:src->{}), after(victim:src->{};tgt->{}; attacker:src->{}; direct:src->{})",
                reactionId,
                (intraTargetMode ? "intraTarget" : "normal"),
                matchedSource,
                matchedTarget,
                consumeSource,
                consumeTarget,
                victim.getId(),
                (attacker != null ? attacker.getId() : -1),
                (direct != null ? direct.getId() : -1),
                (intraTargetMode ? matchedSource : ""),
                beforeVictimSource,
                beforeVictimTarget,
                beforeAttackerSource,
                beforeDirectSource,
                afterVictimSource,
                afterVictimTarget,
                afterAttackerSource,
                afterDirectSource
        );

        // 根据反应ID与命中的方向(src->tgt)执行效果（优先方向化，无则回退全局）
        // elementsAmount 采用被反应元素的“实际消耗量”（before-after），而非配置的目标消耗值
        int actualConsumedTarget = Math.max(0, beforeVictimTarget - afterVictimTarget);
        applyEffectsForReaction(event, reactionId, matchedSource, matchedTarget, attacker, victim, actualConsumedTarget);
    }

    /** 获取实体某元素当前值，实体或元素不存在时返回 -1 */
    private static int getElementValue(LivingEntity entity, String elementId) {
        if (entity == null || elementId == null) return -1;
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
        return container.getValue(elementId);
    }

    /**
     * tick类型反应
     */
    public static void tickTypeReaction(ServerTickEvent.Post event) {
        // 若无任何 tick 类型的反应被加载，直接返回
        if (ElementReactionRegistry.getTickReactions().isEmpty()) return;
        if (event == null) return;

        var server = event.getServer();
        try {
            // 以在线玩家为中心，处理其附近的生物，避免全图全量遍历带来的性能压力
            // 使用集合避免同一实体被多个玩家重复处理
            java.util.Set<Integer> visited = new java.util.HashSet<>();
            for (var player : server.getPlayerList().getPlayers()) {
                if (player == null || player.level().isClientSide()) continue;
                var level = player.level();
                var aabb = player.getBoundingBox().inflate(32.0); // 半径 32 格
                java.util.List<net.minecraft.world.entity.LivingEntity> candidates = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb, e -> e.isAlive());
                for (net.minecraft.world.entity.LivingEntity entity : candidates) {
                    if (entity == null) continue;
                    int id = entity.getId();
                    if (!visited.add(id)) continue; // 已处理过
                    processTickReactionsOnEntity(entity);
                }
            }
        } catch (Throwable t) {
            SpellElemental.LOGGER.warn("tickTypeReaction error: {}", t.getMessage());
        }
    }

    /**
     * 在指定实体上按元素ID扣减元素量，不会降到负数；为 0 时移除该元素条目。
     */
    private static void consumeElementOnEntity(LivingEntity entity, String elementId, int amount) {
        if (entity == null || elementId == null || amount <= 0) return;
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
        int cur = container.getValue(elementId);
        if (cur <= 0) return;
        int next = cur - amount;
        if (next <= 0) {
            container.remove(elementId);
            // 同步到所有客户端：元素被移除（值为0）
            PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), elementId, 0));
        } else {
            container.setValue(elementId, next);
            // 同步到所有客户端：更新后的剩余时长/数值
            PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), elementId, next));
        }
    }

    /**
     * 收集实体上当前存在（值>0）的元素ID列表（小写）。
     */
    private static List<String> collectPresentElements(LivingEntity entity) {
        List<String> out = new ArrayList<>();
        if (entity == null) return out;
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
        Map<String, Integer> snapshot = container.snapshot();
        for (Map.Entry<String, Integer> en : snapshot.entrySet()) {
            if (en.getValue() != null && en.getValue() > 0) {
                out.add(en.getKey().toLowerCase());
            }
        }
        return out;
    }

    /**
     * 基于 DamageSource 的 msgId 粗略推断来源元素：
     * - 将 msgId 小写后，优先在 candidates 中查找作为子串出现的元素名（避免硬编码元素列表）。
     * - 若无匹配则返回 null。
     */
    private static String inferSourceFromDamage(DamageSource source, List<String> candidatesLowercase) {
        if (source == null) return null;
        try {
            String id = source.getMsgId();
            if (id == null || id.isBlank()) return null;
            String low = id.toLowerCase();
            for (String c : candidatesLowercase) {
                if (low.contains(c.toLowerCase())) return c;
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    // -------------- 通用效果执行 --------------

    private static void applyEffectsForReaction(LivingDamageEvent.Pre event, String reactionId,
                                                String matchedSource, String matchedTarget,
                                                Entity attacker, LivingEntity victim,
                                                int elementsAmount) {
        if (reactionId == null) return;
        // 优先方向化效果
        List<ElementReactionRegistry.ReactionEffect> effects = ElementReactionRegistry.getDirectionalEffects(matchedSource, matchedTarget);
        if (effects.isEmpty()) {
            // 回退到全局效果
            effects = ElementReactionRegistry.getEffects(reactionId);
        }
        if (effects.isEmpty()) return;

        float damage = event.getNewDamage();
        for (ElementReactionRegistry.ReactionEffect eff : effects) {
            if (eff == null || eff.type.isEmpty()) continue;
            switch (eff.type) {
                case "damage_amplify" -> {
                    damage = applyDamageAmplify(damage, eff, attacker);
                }
                case "aoe" -> {
                    // 额外溅射伤害：不改变当前 event 的直接伤害，仅对周围单位造成伤害
                    applyAoeEffect(event, damage, eff, attacker, victim, elementsAmount);
                }
                default -> {
                    // 其他类型暂未实现，留作扩展
                }
            }
        }
        if (damage != event.getNewDamage()) {
            event.setNewDamage(damage);
        }
    }

    private static float applyDamageAmplify(float baseDamage, ElementReactionRegistry.ReactionEffect eff, Entity attackerEntity) {
        float out = baseDamage;
        // 基础倍率
        if (eff.multiplier != 0f) {
            out *= eff.multiplier;
        }
        // 公式因子（可选）
        float factor = 1.0f;
        if (eff.formula != null && !eff.formula.isEmpty()) {
            float astral = 0f;
            if (attackerEntity instanceof LivingEntity attacker) {
                astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
            }
            factor *= evaluateFormula(eff.formula, astral);
        }
        return out * factor;
    }

    /**
     * 对受击者周围造成范围伤害。
     * - 使用当前累积的 baseDamage 作为基础，再乘以 eff.multiplier 与公式因子。
     * - 半径 eff.radius <= 0 则不生效。
     * - 伤害来源优先继承 event 的 DamageSource；其他 source 模式暂留扩展。
     */
    private static void applyAoeEffect(LivingDamageEvent.Pre event, float baseDamage,
                                       ElementReactionRegistry.ReactionEffect eff,
                                       Entity attackerEntity, LivingEntity victim,
                                       int elementsAmount) {
        if (victim == null || victim.level().isClientSide()) return;
        float radius = eff.radius;
        if (radius <= 0f) return;

        float finalDamage;
        // 计算最终伤害
        if ("CalculateOverloadDamage".equals(eff.formula)) {
            float astral = 0f;
            if (attackerEntity instanceof LivingEntity attacker) {
                astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
            }
            // 聚变（Overload）反应：当 elementsAmount 为 0 时，默认按 10 计算
            int effectiveAmount = (elementsAmount == 0 ? 10 : elementsAmount);
            finalDamage = Math.max(0f, ReactionInjuryFormula.CalculateOverloadDamage((float) effectiveAmount, eff.multiplier, astral));
        } else {
            // 通用：baseDamage * multiplier * formulaFactor
            float out = baseDamage;
            if (eff.multiplier != 0f) out *= eff.multiplier;
            float factor = 1.0f;
            if (eff.formula != null && !eff.formula.isEmpty()) {
                float astral = 0f;
                if (attackerEntity instanceof LivingEntity attacker) {
                    astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
                }
                factor *= evaluateFormula(eff.formula, astral);
            }
            finalDamage = Math.max(0f, out * factor);
        }
        if (finalDamage <= 0f) return;

        // 伤害类型处理：若配置了 damage_type，按该类型构造 DamageSource，否则继承原事件
        DamageSource src = event.getSource();
        if (eff.damageType != null && !eff.damageType.isEmpty()) {
            try {
                ResourceLocation rl = ResourceLocation.tryParse(eff.damageType);
                if (rl == null) {
                    throw new IllegalArgumentException("Invalid damage_type RL: " + eff.damageType);
                }
                var reg = victim.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                var key = ResourceKey.create(Registries.DAMAGE_TYPE, rl);
                Holder<DamageType> holder = reg.getHolder(key).orElse(null);
                if (holder != null) {
                    Entity direct = event.getSource().getDirectEntity();
                    Entity causing = (attackerEntity != null) ? attackerEntity : event.getSource().getEntity();
                    src = new DamageSource(holder, direct, causing);
                }
            } catch (Throwable t) {
                // 解析失败时回退继承
            }
        }

        // 查找半径内目标（可配置是否包含受击者/攻击者）
        var aabb = victim.getBoundingBox().inflate(radius);
        final DamageSource srcFinal = src;
        final LivingEntity attackerLiving = (attackerEntity instanceof LivingEntity le) ? le : null;
        for (LivingEntity ent : victim.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> e.isAlive())) {
            if (!eff.damageVictim && ent == victim) continue;
            if (!eff.damageAttacker && attackerLiving != null && ent == attackerLiving) continue;
            if (ent.invulnerableTime > 0) continue;
            if (!eff.attachElement) {
                // 抑制“受伤即附着”逻辑
                DamageAttachmentGuards.runAsNonAttachable(() -> ent.hurt(srcFinal, finalDamage));
            } else {
                ent.hurt(srcFinal, finalDamage);
            }
        }
    }

    /**
     * 在单个实体上处理所有已注册的 tick 反应：
     * - 若满足 requirements（元素值>=阈值）则按配置消耗（不会为负）；
     * - 计算本次总实际消耗量 totalConsumed（所有元素之和），供公式型效果使用；
     * - 执行可选 effects（当前支持 aoe）。
     */
    private static void processTickReactionsOnEntity(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;
        var allTick = ElementReactionRegistry.getTickReactions();
        if (allTick.isEmpty()) return;

        // 读取元素容器
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);

        long time = entity.level().getGameTime();
        for (String rid : allTick) {
            ElementReactionRegistry.TickRule rule = ElementReactionRegistry.getTickRule(rid);
            if (rule == null) continue;
            // 基于规则的触发间隔进行节流（interval=1 表示每tick）
            if (rule.interval > 1 && (time % rule.interval) != 0) {
                continue;
            }
            // 检查 requirements：仅当所有需求元素的值 >= 阈值时才触发
            boolean meetsRequirements = true;
            for (java.util.Map.Entry<String, Integer> req : rule.requirements.entrySet()) {
                String elem = req.getKey();
                int need = Math.max(0, req.getValue());
                if (need <= 0) continue; // 0 或负数视为无需求
                int cur = container.getValue(elem);
                if (cur < need) {
                    meetsRequirements = false;
                    break;
                }
            }
            if (!meetsRequirements) {
                continue;
            }
            // 调试：记录满足 requirements 的 tick 反应
            try {
                SpellElemental.LOGGER.debug("[TickReaction] id=" + rid + " meets requirements=" + rule.requirements + ", consumePlan=" + rule.consume + ", time=" + time);
            } catch (Throwable ignored) {}
            int totalConsumed = 0;
            for (java.util.Map.Entry<String, Integer> con : rule.consume.entrySet()) {
                String elem = con.getKey();
                int want = Math.max(0, con.getValue());
                int before = container.getValue(elem);
                if (before <= 0 || want <= 0) continue;
                int real = Math.min(before, want);
                if (real > 0) {
                    consumeElementOnEntity(entity, elem, real);
                    totalConsumed += real;
                }
            }

            // 执行可选效果：允许 totalConsumed == 0 也触发（例如配置为零消耗时）
            if (!rule.effects.isEmpty()) {
                try {
                    SpellElemental.LOGGER.debug("[TickReaction] id=" + rid + " totalConsumed=" + totalConsumed + ", start effects size=" + rule.effects.size());
                } catch (Throwable ignored) {}
                for (ElementReactionRegistry.ReactionEffect eff : rule.effects) {
                    if (eff == null || eff.type.isEmpty()) continue;
                    if ("aoe".equals(eff.type)) {
                        applyAoeEffectFromTick(entity, eff, totalConsumed);
                    } else if ("extra".equals(eff.type)) {
                        applyExtraDamageFromTick(entity, eff, totalConsumed);
                    }
                    // 其他类型留作扩展
                }
            }
        }
    }

    /**
     * tick 触发的 AOE 伤害（无事件上下文）：
     * - elementsAmount: 本次总实际消耗量
     * - 若指定公式：恒使用星相加成（遵从用户偏好，移除 use_astral 配置）
     */
    private static void applyAoeEffectFromTick(LivingEntity center, ElementReactionRegistry.ReactionEffect eff, int elementsAmount) {
        if (center == null || center.level().isClientSide()) return;
        float radius = eff.radius;
        if (radius <= 0f) return;

        // 计算最终伤害
        float finalDamage;
        float astral = 0f;
        if (center instanceof LivingEntity attacker) {
            astral = (float) attacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
        }
        if (eff.formula != null && !eff.formula.isEmpty()) {
            if ("CalculateOverloadDamage".equals(eff.formula)) {
                // 聚变（Overload）反应：当 elementsAmount 为 0 时，默认按 10 计算
                int effectiveAmount = (elementsAmount == 0 ? 10 : elementsAmount);
                finalDamage = Math.max(0f, ReactionInjuryFormula.CalculateOverloadDamage((float) effectiveAmount, eff.multiplier, astral));
            } else {
                float out = (float) elementsAmount;
                if (eff.multiplier != 0f) out *= eff.multiplier;
                float factor = evaluateFormula(eff.formula, astral);
                finalDamage = Math.max(0f, out * factor);
            }
        } else {
            float out = (float) elementsAmount;
            if (eff.multiplier != 0f) out *= eff.multiplier;
            finalDamage = Math.max(0f, out);
        }
        if (finalDamage <= 0f) return;

        // 构造 DamageSource：优先使用配置的 damage_type，否则放弃（tick 无原事件可继承）
        DamageSource src = null;
        try {
            if (eff.damageType != null && !eff.damageType.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(eff.damageType);
                if (rl != null) {
                    var reg = center.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                    var key = ResourceKey.create(Registries.DAMAGE_TYPE, rl);
                    Holder<DamageType> holder = reg.getHolder(key).orElse(null);
                    if (holder != null) {
                        // AOE(tick) 保持原有语义：direct=center, attacker=center
                        src = new DamageSource(holder, center, center);
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (src == null) return;

        // 调试：打印 AOE 伤害信息
        try {
            SpellElemental.LOGGER.debug("[TickAOE] center=" + center.getName().getString() + 
                    ", elementsAmount=" + elementsAmount + ", astral=" + astral + 
                    ", formula=" + eff.formula + ", multiplier=" + eff.multiplier + 
                    ", radius=" + radius + ", dmgType=" + eff.damageType + ", finalDamage=" + finalDamage +
                    ", attachElement=" + eff.attachElement + ", damageAttacker=" + eff.damageAttacker);
        } catch (Throwable ignored) {}

        var aabb = center.getBoundingBox().inflate(radius);
        final DamageSource srcFinal = src;
        for (LivingEntity ent : center.level().getEntitiesOfClass(LivingEntity.class, aabb, e -> e.isAlive())) {
            // tick 上下文中，使用 damageAttacker 控制是否伤害中心实体；damageVictim 无语义
            if (!eff.damageAttacker && ent == center) continue;
            if (ent.invulnerableTime > 0) continue;
            if (!eff.attachElement) {
                DamageAttachmentGuards.runAsNonAttachable(() -> ent.hurt(srcFinal, finalDamage));
            } else {
                ent.hurt(srcFinal, finalDamage);
            }
        }
    }

    /**
     * tick 触发的额外单体伤害：对中心实体(center)额外造成一段伤害。
     * - 使用 elementsAmount 参与公式/倍率计算；当为 Overload 且 elementsAmount==0 时，默认按 10 计算。
     * - 必须提供 damage_type 才会结算（与 tick AOE 保持一致）。
     */
    private static void applyExtraDamageFromTick(LivingEntity center, ElementReactionRegistry.ReactionEffect eff, int elementsAmount) {
        if (center == null || center.level().isClientSide()) return;
        // 计算伤害：优先使用最近攻击者的星耀祝福，否则回退为中心实体自身
        LivingEntity approxAttacker = center.getLastHurtByMob();
        float astral = (float) (approxAttacker != null
                ? approxAttacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING)
                : center.getAttributeValue(ModAttributes.ASTRAL_BLESSING));
        float finalDamage;
        if (eff.formula != null && !eff.formula.isEmpty()) {
            if ("CalculateOverloadDamage".equals(eff.formula)) {
                int effectiveAmount = (elementsAmount == 0 ? 10 : elementsAmount);
                finalDamage = Math.max(0f, ReactionInjuryFormula.CalculateOverloadDamage((float) effectiveAmount, eff.multiplier, astral));
            } else {
                float out = (float) elementsAmount;
                if (eff.multiplier != 0f) out *= eff.multiplier;
                float factor = evaluateFormula(eff.formula, astral);
                finalDamage = Math.max(0f, out * factor);
            }
        } else {
            float out = (float) elementsAmount;
            if (eff.multiplier != 0f) out *= eff.multiplier;
            finalDamage = Math.max(0f, out);
        }
        if (finalDamage <= 0f) return;

        // 构造 DamageSource（必须提供）
        DamageSource src = null;
        try {
            if (eff.damageType != null && !eff.damageType.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(eff.damageType);
                if (rl != null) {
                    var reg = center.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                    var key = ResourceKey.create(Registries.DAMAGE_TYPE, rl);
                    Holder<DamageType> holder = reg.getHolder(key).orElse(null);
                    if (holder != null) {
                        // direct 实体为中心实体（被击中者），attacker 使用近似攻击者（若不存在则回退中心）
                        Entity attackerEntity = (approxAttacker != null ? approxAttacker : center);
                        src = new DamageSource(holder, center, attackerEntity);
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (src == null) return;

        // 直接对中心实体结算伤害
        final DamageSource srcFinal = src;
        if (!eff.attachElement) {
            DamageAttachmentGuards.runAsNonAttachable(() -> center.hurt(srcFinal, finalDamage));
        } else {
            center.hurt(srcFinal, finalDamage);
        }

        // 调试：打印 extra 伤害信息
        try {
            String attackerName = (approxAttacker != null ? approxAttacker.getName().getString() : "null");
            float attackerAstral = (float) (approxAttacker != null ? approxAttacker.getAttributeValue(ModAttributes.ASTRAL_BLESSING) : -1);
            float centerAstral = (float) center.getAttributeValue(ModAttributes.ASTRAL_BLESSING);
            SpellElemental.LOGGER.debug("[TickExtra] center=" + center.getName().getString() + 
                    ", approxAttacker=" + attackerName + ", approxAttackerAstral=" + attackerAstral + 
                    ", centerAstral=" + centerAstral + 
                    ", elementsAmount=" + elementsAmount + ", formula=" + eff.formula + 
                    ", multiplier=" + eff.multiplier + ", dmgType=" + eff.damageType + 
                    ", finalDamage=" + finalDamage + ", attachElement=" + eff.attachElement);
        } catch (Throwable ignored) {}
    }

    private static float evaluateFormula(String formula, float astral) {
        // 目前支持的公式：AmplifiedReactionBonus(astral)
        if ("AmplifiedReactionBonus".equals(formula)) {
            return ReactionInjuryFormula.AmplifiedReactionBonus(astral);
        }
        return 1.0f;
    }
}