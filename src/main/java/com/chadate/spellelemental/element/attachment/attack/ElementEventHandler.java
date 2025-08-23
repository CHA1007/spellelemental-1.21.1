package com.chadate.spellelemental.element.attachment.attack;



import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.config.ServerConfig;
import com.chadate.spellelemental.network.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.data.UnifiedElementAttachmentAssets;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ElementEventHandler {

    public static void handleElementAttachment(SpellDamageEvent event) {
        // 只在服务端处理元素附着，客户端通过网络同步获取
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity target = event.getEntity();
        String spellId = event.getSpellDamageSource().spell().getSpellId();
        String school = event.getSpellDamageSource().spell().getSchoolType().getId().toString();

        // 调试：记录元素附着处理开始时的元素状态
        ElementContainerAttachment container = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
        SpellElemental.LOGGER.info("[DEBUG] ElementAttachment START - target {} elements before: {}", 
            target.getId(), 
            container.snapshot());

        SpellElemental.LOGGER.info("[SpellElemental] Resolved spell '{}' school: {}", spellId, school);

        // 优先检查配置中的法术专属元素附着
        String elementId = null;
        ResourceLocation spellKey = ResourceLocation.tryParse(spellId);
        if (spellKey != null) {
            elementId = ServerConfig.getSpellElementOverride(spellKey);
            if (elementId != null) {
                SpellElemental.LOGGER.info("[SpellElemental] Using custom element '{}' for spell '{}'", elementId, spellId);
            }
        }
        
        // 如果没有配置专属元素，则回退到基于学派的映射
        if (elementId == null || elementId.isBlank()) {
            elementId = UnifiedElementAttachmentAssets.getElementIdBySchool(school);
            if (elementId == null || elementId.isBlank()) {
                SpellElemental.LOGGER.debug("[SpellElemental] No element mapping found for spell '{}' with school '{}'", spellId, school);
                return;
            }
            SpellElemental.LOGGER.debug("[SpellElemental] Using school-based element '{}' for spell '{}'", elementId, spellId);
        }

        // 依据配置的每法术覆盖量计算时长（找不到则回退默认）
        int duration = ServerConfig.ELEMENT_ATTACHMENT_DEFAULT.get();
        if (spellKey != null) {
            duration = ServerConfig.getSpellAttachmentAmount(spellKey);
        }

        // 获取攻击者信息用于追踪
        Entity attacker = event.getSpellDamageSource().getEntity();
        int attackerId = (attacker != null) ? attacker.getId() : -1;

        // 双重ICD：每N次命中或每T刻
        int step = ServerConfig.getIcdHitStep();
        int timeTicks = ServerConfig.getIcdTimeTicks();
        long now = target.level().getGameTime();
        boolean allow = SpellIcdTracker.allowAndRecord(attacker, target, spellKey, now, step, timeTicks);
        if (!allow) {
            SpellElemental.LOGGER.debug("[SpellElemental][ICD] Blocked apply: attacker={}, spell={}, step={}, time={}t", attackerId, spellId, step, timeTicks);
            return;
        }

        applyAttachment(target, elementId.toLowerCase(), duration, attackerId);
        
        // 调试：记录元素附着处理完成后的元素状态
        ElementContainerAttachment containerAfter = target.getData(SpellAttachments.ELEMENTS_CONTAINER);
        SpellElemental.LOGGER.info("[DEBUG] ElementAttachment END - target {} elements after: {}", 
            target.getId(), 
            containerAfter.snapshot());
    }

    private static void applyAttachment(LivingEntity entity, String elementKeyLower, int duration, int attackerId) {
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
        container.setValue(elementKeyLower, duration);
        long gameTime = entity.level().getGameTime();
        // 记录攻击者信息用于tick反应追踪
        container.markAppliedWithAttacker(elementKeyLower, gameTime, attackerId);
        ElementDecaySystem.track(entity);
        // 只向能看到该实体的玩家发送元素数据
        SpellElemental.LOGGER.info("[DEBUG] Sending element attachment sync - Entity: {}, Element: {}, Duration: {}", 
            entity.getId(), elementKeyLower, duration);
        PacketDistributor.sendToPlayersTrackingEntity(entity, new ElementData(entity.getId(), elementKeyLower, duration));
        
        // 额外：向附近的玩家强制同步（防止追踪范围问题）
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
                double distance = player.distanceTo(entity);
                if (distance <= 64.0) {
                    PacketDistributor.sendToPlayer(player, new ElementData(entity.getId(), elementKeyLower, duration));
                    SpellElemental.LOGGER.info("[DEBUG] Force syncing to nearby player: {} (distance: {:.1f})", 
                        player.getName().getString(), distance);
                }
            }
        }
    }

    // 当玩家开始追踪（看见）某个实体时，同步该实体的元素状态快照
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) return;
        ElementContainerAttachment container = living.getData(SpellAttachments.ELEMENTS_CONTAINER);
        var snap = container.snapshot();
        String[] keys = snap.keySet().toArray(new String[0]);
        int[] values = new int[keys.length];
        for (int i = 0; i < keys.length; i++) values[i] = snap.get(keys[i]);
        PacketDistributor.sendToPlayer(player, new ElementData.ElementSnapshot(living.getId(), keys, values));
        SpellElemental.LOGGER.info("[DEBUG] Syncing elements to client - Player: {}, Target: {}, Elements: {}", 
            player.getName().getString(), living.getId(), snap);
    }
}