package com.chadate.spellelemental.entity;

import com.chadate.spellelemental.data.SpellAttachments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class CustomFreezeController {
    // 每tick衰减的冻结值
    private static final int FREEZE_DECAY_PER_TICK = 4;

    public static void CheckFreezeStatus(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        // 检查是否应该被冻结
        shouldBeFrozen(target);
        // 处理冻结效果
        handleFreezeEffect(event, target);
    }
    
    /**
     * 处理冻结效果
     * @param event 实体tick事件
     * @param entity 要处理的实体
     */
    private static void handleFreezeEffect(EntityTickEvent.Pre event, LivingEntity entity) {
        if (!isFrozen(entity)) return;
        
        // 获取当前冻结时间
        int currentDuration = getFreezeDuration(entity);
        
        // 每tick减少冻结值
        int newDuration = Math.max(currentDuration - FREEZE_DECAY_PER_TICK, 0);
        entity.getData(SpellAttachments.FREEZE_ELEMENT).setValue(newDuration);
        // 如果冻结时间结束，移除冻结效果
        if (newDuration == 0) {
            removeFreeze(entity);
            return;
        }
        
        // 如果是生物，禁用AI
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
    }
    
    /**
     * 检查实体是否应该被冻结
     * 当实体同时具有冰元素和水元素，且当前未被冻结时，应该被冻结
     * @param entity 要检查的实体
     */
    public static void shouldBeFrozen(LivingEntity entity) {
        // 检查实体是否已经处于冻结状态
        if (isFrozen(entity)) {
             return;
        }
        
        // 检查实体是否同时具有冰元素和水元素
        int iceElement = entity.getData(SpellAttachments.ICE_ELEMENT).getValue();
        int waterElement = entity.getData(SpellAttachments.WATER_ELEMENT).getValue();
        
        if (iceElement > 0 && waterElement > 0) {
            // 获取冻结抗性层数
            int freezeLayers = getFreezeLayers(entity);
            
            // 计算实际冻结时间
            int totalDuration = iceElement + waterElement;
            int actualDuration = (int) (totalDuration * (1 - freezeLayers * 0.1));
            
            // 如果实际冻结时间大于0，则应该被冻结
            if (actualDuration > 0) {
                // 应用冻结效果
                applyFreeze(entity, freezeLayers);
            }
        }

    }
    
    /**
     * 对实体施加冻结效果
     * @param entity 要冻结的实体
     * @param freezeLayers 冻结抗性层数
     */
    public static void applyFreeze(LivingEntity entity, int freezeLayers) {
        // 获取冰元素和水元素的持续时间
        int iceDuration = entity.getData(SpellAttachments.ICE_ELEMENT).getValue();
        int waterDuration = entity.getData(SpellAttachments.WATER_ELEMENT).getValue();
        
        // 计算总冻结时间（冰元素和水元素持续时间之和）
        int totalDuration = iceDuration + waterDuration;
        
        // 根据抗性层数计算实际冻结时间
        int actualDuration = (int) (totalDuration * (1 - freezeLayers * 0.1));
        
        // 设置冻结元素数据
        entity.getData(SpellAttachments.FREEZE_ELEMENT).setValue(actualDuration);
        
        // 对玩家施加移动减速效果
        if (entity instanceof Player) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, actualDuration, 255, false, false, false));
        } else if (entity instanceof Mob) {
            // 初始时禁用生物的AI
            ((Mob) entity).setNoAi(true);
        }
        
        // 更新冻结抗性层数
        if (freezeLayers > 0) {
            int newFreezeLayers = Math.min(freezeLayers + 1, 5);
            entity.getData(SpellAttachments.FREEZE_LAYERS).setValue(newFreezeLayers);
        } else {
            entity.getData(SpellAttachments.FREEZE_LAYERS).setValue(1);
        }
        // 清除冰元素和水元素
        entity.getData(SpellAttachments.ICE_ELEMENT).setValue(0);
        entity.getData(SpellAttachments.WATER_ELEMENT).setValue(0);
    }
    
    /**
     * 移除实体的冻结效果
     * @param entity 要解除冻结的实体
     */
    public static void removeFreeze(LivingEntity entity) {
        // 移除冻结元素数据
        entity.removeData(SpellAttachments.FREEZE_ELEMENT);
        
        // 移除玩家的移动减速效果
        if (entity instanceof Player) {
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        } else if (entity instanceof Mob) {
            // 重新启用生物的AI
            ((Mob) entity).setNoAi(false);
        }
    }
    
    /**
     * 检查实体是否处于冻结状态
     * @param entity 要检查的实体
     * @return 如果实体被冻结则返回true，否则返回false
     */
    public static boolean isFrozen(LivingEntity entity) {
        return entity.getData(SpellAttachments.FREEZE_ELEMENT).getValue() > 0;
    }
    
    /**
     * 获取实体剩余的冻结时间
     * @param entity 要检查的实体
     * @return 剩余的冻结时间（以游戏刻为单位），如果未冻结则返回0
     */
    public static int getFreezeDuration(LivingEntity entity) {
        return entity.getData(SpellAttachments.FREEZE_ELEMENT).getValue();
    }
    
    /**
     * 获取实体的冻结抗性层数
     * @param entity 要检查的实体
     * @return 冻结抗性层数
     */
    public static int getFreezeLayers(LivingEntity entity) {
        return entity.getData(SpellAttachments.FREEZE_LAYERS).getValue();
    }
}
