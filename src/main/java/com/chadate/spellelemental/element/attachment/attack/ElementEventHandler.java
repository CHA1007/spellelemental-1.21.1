package com.chadate.spellelemental.element.attachment.attack;


import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.attachment.config.UnifiedElementAttachmentConfig;
import com.chadate.spellelemental.element.attachment.data.EnvironmentalAttachmentRegistry;
import com.chadate.spellelemental.element.reaction.runtime.ElementReactionHandler;
import com.chadate.spellelemental.event.element.ElementDecaySystem;
import com.chadate.spellelemental.util.DamageAttachmentGuards;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ElementEventHandler {
    public static void handleElementAttachment(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        int entityId = target.getId();
        float damageAmount = event.getNewDamage();
        // 统一的“不可附着伤害”早退：当反应伤害或其他标记的伤害到达时，不触发受伤即附着
        if (DamageAttachmentGuards.isNonAttachable()) {
            return;
        }
        ElementAttachmentRegistry.handleAttachment(target, source, entityId, damageAmount);

        ElementReactionHandler.tryAmplifyAnyReaction(event);
    }
    private ElementEventHandler() {}

    public static void handleEnvironmentalAttachment(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        List<UnifiedElementAttachmentConfig> configs = EnvironmentalAttachmentRegistry.getAll();
        if (configs.isEmpty()) return;

        for (ServerLevel srv : event.getServer().getAllLevels()) {
            srv.getEntities().getAll().forEach(entity -> {
                if (!(entity instanceof LivingEntity living)) return;
                for (UnifiedElementAttachmentConfig cfg : configs) {
                    UnifiedElementAttachmentConfig.EnvironmentalConditions env = cfg.getEnvironmentalConditions();
                    if (env == null || env.getWaterConditions() == null) continue;
                    int interval = Math.max(1, env.getCheckInterval());
                    if (living.tickCount % interval != 0) continue;
                    boolean needInWater = env.getWaterConditions().isInWater();
                    boolean needInRain = env.getWaterConditions().isInRain();
                    boolean ok = needInWater && living.isInWaterOrBubble();
                    if (needInRain && living.level().isRainingAt(living.blockPosition())) ok = true;
                    if (!ok) continue;
                    applyAttachment(living, cfg);
                }
            });
        }
    }

    private static void applyAttachment(LivingEntity entity, UnifiedElementAttachmentConfig cfg) {
        ElementContainerAttachment container = entity.getData(SpellAttachments.ELEMENTS_CONTAINER);
        int duration = cfg.getEffects() != null ? cfg.getEffects().getDuration() : 200;
        String elementKey = extractElementKey(cfg.getAttachmentType());
        container.setValue(elementKey, duration);
        // 跟踪衰减，离开环境后由衰减系统自然清除
        ElementDecaySystem.track(entity);
        PacketDistributor.sendToAllPlayers(new ElementData(entity.getId(), elementKey, duration));
    }

    private static String extractElementKey(String attachmentTypeName) {
        if (attachmentTypeName == null) return "";
        String s = attachmentTypeName.contains(":" ) ? attachmentTypeName.substring(attachmentTypeName.indexOf(":" ) + 1) : attachmentTypeName;
        return s.toLowerCase();
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
    }
}