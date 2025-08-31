package com.chadate.spellelemental.event;

import com.chadate.spellelemental.config.ServerConfig;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.attack.SpellIcdTracker;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import com.chadate.spellelemental.network.ElementData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 剑油攻击事件处理器
 * 处理每次攻击时消耗剑上的元素量
 */
@EventBusSubscriber
public class SwordOilAttackHandler {
    
    // 基础元素消耗量范围：20~200点（根据蓄力力度调整）
    private static final int MIN_ELEMENT_CONSUMPTION = 20;
    private static final int MAX_ELEMENT_CONSUMPTION = 200;
    
    // 用于存储主攻击的伤害值，以便计算横扫比例
    private static final java.util.Map<Integer, Float> mainAttackDamageMap = new java.util.concurrent.ConcurrentHashMap<>();
    
    // 用于累积横扫攻击的总元素消耗量
    private static final java.util.Map<Integer, Integer> sweepElementConsumptionMap = new java.util.concurrent.ConcurrentHashMap<>();
    
    // 用于记录主目标实体ID，避免在横扫事件中重复处理
    private static final java.util.Map<Integer, Integer> mainTargetMap = new java.util.concurrent.ConcurrentHashMap<>();
    
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        
        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }
        
        // 检查是否为剑类武器
        if (!(weapon.getItem() instanceof SwordItem)) {
            return;
        }
        
        // 检查被攻击的实体是否为生物
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        
        // 检查剑是否有元素附着
        CompoundTag customData = weapon.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = customData.getCompound("ElementAttachment");
        
        if (!elementData.contains("element") || !elementData.contains("amount")) {
            return;
        }
        
        String elementType = elementData.getString("element");
        int currentAmount = elementData.getInt("amount");
        
        // 记录主攻击的基础伤害（用于横扫比例计算）
        float baseDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        mainAttackDamageMap.put(player.getId(), baseDamage);
        
        // 初始化横扫元素消耗累积器
        sweepElementConsumptionMap.put(player.getId(), 0);
        
        // 记录主目标实体ID，避免在横扫事件中重复处理
        mainTargetMap.put(player.getId(), target.getId());
        
        // 获取玩家的攻击蓄力力度（0.0 到 1.0）
        float chargeRatio = player.getAttackStrengthScale(0.5f);
        
        // 为被攻击的实体附着元素
        applyElementToTarget(target, elementType, player.getId(), chargeRatio);
        
        // 计算主目标的元素消耗量
        int mainElementConsumption = Math.round(MIN_ELEMENT_CONSUMPTION + 
            (MAX_ELEMENT_CONSUMPTION - MIN_ELEMENT_CONSUMPTION) * chargeRatio);
        mainElementConsumption = Math.max(MIN_ELEMENT_CONSUMPTION, Math.min(MAX_ELEMENT_CONSUMPTION, mainElementConsumption));
        
        // 延迟处理元素消耗，等待横扫攻击完成
        // 使用调度器在下一个tick处理总消耗
        scheduleElementConsumption(player, weapon, currentAmount, mainElementConsumption);
        

    }
    
    /**
     * 处理横扫攻击时的元素附着
     * 监听所有伤害事件，检测是否为剑油武器造成的伤害
     * 使用 HIGH 优先级确保在元素反应检测之前完成元素附着
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // 只在服务端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        // 获取攻击者
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }
        
        ItemStack weapon = attacker.getMainHandItem();
        
        // 检查是否为剑类武器
        if (!(weapon.getItem() instanceof SwordItem)) {
            return;
        }
        
        // 检查剑是否有元素附着
        CompoundTag customData = weapon.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = customData.getCompound("ElementAttachment");
        
        if (!elementData.contains("element") || !elementData.contains("amount")) {
            return;
        }
        
        String elementType = elementData.getString("element");
        
        // 检查是否为主目标，如果是则跳过（已在AttackEntityEvent中处理）
        Integer mainTargetId = mainTargetMap.get(attacker.getId());
        if (mainTargetId != null && mainTargetId.equals(event.getEntity().getId())) {
            return; // 主目标已在AttackEntityEvent中处理，跳过
        }
        
        // 获取玩家的攻击蓄力力度（0.0 到 1.0）
        float chargeRatio = attacker.getAttackStrengthScale(0.5f);
        
        // 获取当前伤害值和主攻击基础伤害
        float currentDamage = event.getNewDamage();
        Float mainDamage = mainAttackDamageMap.get(attacker.getId());
        
        // 计算伤害比例（横扫伤害通常是主伤害的一部分）
        float damageRatio = 1.0f; // 默认比例
        if (mainDamage != null && mainDamage > 0) {
            damageRatio = Math.min(1.0f, currentDamage / mainDamage);
            // 确保比例在合理范围内（至少10%）
            damageRatio = Math.max(0.1f, damageRatio);
        }
        
        // 为被攻击的实体附着元素（根据伤害比例调整）
        int consumedAmount = applyElementToTargetWithRatio(event.getEntity(), elementType, attacker.getId(), chargeRatio, damageRatio);
        
        // 累积横扫元素消耗量
        sweepElementConsumptionMap.merge(attacker.getId(), consumedAmount, Integer::sum);
    }
    
    /**
     * 为目标实体附着元素
     * 用于横扫攻击时的额外目标，根据伤害比例调整元素量
     * @return 实际消耗的元素量
     */
    private static int applyElementToTargetWithRatio(LivingEntity target, String elementType, int attackerId, float chargeRatio, float damageRatio) {
        // 获取攻击者实体用于ICD检查
        LivingEntity attacker = null;
        if (target.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(attackerId) instanceof LivingEntity livingAttacker) {
                attacker = livingAttacker;
            }
        }
        
        // 应用ICD机制检查 - 使用剑油横扫作为"法术"标识
        net.minecraft.resources.ResourceLocation swordOilSweepSpellId = 
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("spellelemental", "sword_oil_sweep_" + elementType.toLowerCase());
        
        int step = ServerConfig.getIcdHitStep();
        int timeTicks = ServerConfig.getIcdTimeTicks();
        long currentTick = target.level().getGameTime();
        
        boolean allowAttachment = SpellIcdTracker.allowAndRecord(attacker, target, swordOilSweepSpellId, currentTick, step, timeTicks);
        if (!allowAttachment) {
            return 0; // ICD未满足，跳过此次元素附着，返回0消耗量
        }
        
        // 根据蓄力力度计算基础元素附着量（20~200点）
        int baseElementAmount = Math.round(20 + (200 - 20) * chargeRatio);
        baseElementAmount = Math.max(20, Math.min(200, baseElementAmount));
        
        // 根据伤害比例调整元素附着量
        int elementAmount = Math.round(baseElementAmount * damageRatio);
        // 确保最小元素量为基础量的10%
        int minAmount = Math.max(2, baseElementAmount / 10);
        elementAmount = Math.max(minAmount, elementAmount);
        
        ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
        container.setValue(elementType.toLowerCase(), elementAmount);
        long gameTime = target.level().getGameTime();
        
        // 记录攻击者信息用于反应追踪
        container.markAppliedWithAttacker(elementType.toLowerCase(), gameTime, attackerId);
        ElementDecaySystem.track(target);
        
        // 向附近的玩家同步元素状态
        if (target.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                double distance = player.distanceTo(target);
                if (distance <= 64.0) {
                    PacketDistributor.sendToPlayer(player, new ElementData(target.getId(), elementType.toLowerCase(), elementAmount));
                }
            }
        }
        return elementAmount;
    }
    
    /**
     * 为目标实体附着元素
     * @param target 目标实体
     * @param elementType 元素类型
     * @param attackerId 攻击者ID
     * @param chargeRatio 蓄力力度（0.0-1.0）
     */
    private static void applyElementToTarget(LivingEntity target, String elementType, int attackerId, float chargeRatio) {
        // 获取攻击者实体用于ICD检查
        LivingEntity attacker = null;
        if (target.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(attackerId) instanceof LivingEntity livingAttacker) {
                attacker = livingAttacker;
            }
        }
        
        // 应用ICD机制检查 - 使用剑油作为"法术"标识
        net.minecraft.resources.ResourceLocation swordOilSpellId = 
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("spellelemental", "sword_oil_" + elementType.toLowerCase());
        
        int step = ServerConfig.getIcdHitStep();
        int timeTicks = ServerConfig.getIcdTimeTicks();
        long currentTick = target.level().getGameTime();
        
        boolean allowAttachment = SpellIcdTracker.allowAndRecord(attacker, target, swordOilSpellId, currentTick, step, timeTicks);
        if (!allowAttachment) {
            return; // ICD未满足，跳过此次元素附着
        }
        
        // 根据蓄力力度计算元素附着量（20~200点）
        // chargeRatio: 0.0 -> 20点, 1.0 -> 200点
        int elementAmount = Math.round(20 + (200 - 20) * chargeRatio);
        
        // 确保元素量在有效范围内
        elementAmount = Math.max(20, Math.min(200, elementAmount));
        
        ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
        container.setValue(elementType.toLowerCase(), elementAmount);
        long gameTime = target.level().getGameTime();
        
        // 记录攻击者信息用于反应追踪
        container.markAppliedWithAttacker(elementType.toLowerCase(), gameTime, attackerId);
        ElementDecaySystem.track(target);
        
        // 向附近的玩家同步元素状态
        if (target.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                double distance = player.distanceTo(target);
                if (distance <= 64.0) {
                    PacketDistributor.sendToPlayer(player, new ElementData(target.getId(), elementType.toLowerCase(), elementAmount));
                }
            }
        }
    }
    
    /**
     * 延迟处理元素消耗，等待横扫攻击完成
     */
    private static void scheduleElementConsumption(Player player, ItemStack weapon, int currentAmount, int mainElementConsumption) {
        // 使用服务器调度器在下一个tick处理
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                // 获取累积的横扫元素消耗量
                Integer sweepConsumption = sweepElementConsumptionMap.remove(player.getId());
                if (sweepConsumption == null) sweepConsumption = 0;
                
                // 计算总消耗量
                int totalConsumption = mainElementConsumption + sweepConsumption;
                
                // 更新剑油元素量
                updateWeaponElementAmount(weapon, currentAmount, totalConsumption);
                
                // 清理缓存
                mainAttackDamageMap.remove(player.getId());
                mainTargetMap.remove(player.getId());
            });
        }
    }
    
    /**
     * 更新剑油元素量
     */
    private static void updateWeaponElementAmount(ItemStack weapon, int currentAmount, int totalConsumption) {
        CompoundTag customData = weapon.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = customData.getCompound("ElementAttachment");
        
        int newAmount = Math.max(0, currentAmount - totalConsumption);
        
        if (newAmount == 0) {
            // 元素量耗尽，移除元素附着
            customData.remove("ElementAttachment");
        } else {
            // 更新元素量
            elementData.putInt("amount", newAmount);
            customData.put("ElementAttachment", elementData);
        }
        
        // 保存更新后的数据
        weapon.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.of(customData));
    }
}
