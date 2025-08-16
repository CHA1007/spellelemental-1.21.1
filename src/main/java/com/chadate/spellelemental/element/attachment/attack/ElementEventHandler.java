package com.chadate.spellelemental.element.attachment.attack;


import com.chadate.spellelemental.client.network.custom.ElementData;
import com.chadate.spellelemental.data.ElementContainerAttachment;
import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.element.reaction.runtime.ElementReactionHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ElementEventHandler {
    @SubscribeEvent
    public static void handleElementAttachment(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        int entityId = target.getId();
        float damageAmount = (float) event.getNewDamage();

        ElementAttachmentRegistry.handleAttachment(target, source, entityId, damageAmount);

        ElementReactionHandler.tryAmplifyAnyReaction(event);
    }

    // 当玩家开始追踪（看见）某个实体时，同步该实体的元素状态快照
    @SubscribeEvent
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