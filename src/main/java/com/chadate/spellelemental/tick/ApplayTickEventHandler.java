package com.chadate.spellelemental.tick;

import com.chadate.spellelemental.data.SpellAttachments;
import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.entity.CustomFreezeController;
import com.chadate.spellelemental.event.custom.TickEvent;
import com.chadate.spellelemental.network.custom.ElementData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApplayTickEventHandler {
    @SubscribeEvent
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        int entityId = entity.getId();
        if (entity.isInWaterOrRain()) {
            entity.getData(SpellAttachments.WATER_ELEMENT).setValue(200);
        }
        processElement(entity, entityId, SpellAttachments.FIRE_ELEMENT, "fire_element");
        processElement(entity, entityId, SpellAttachments.ICE_ELEMENT, "ice_element");
        processElement(entity, entityId, SpellAttachments.LIGHTNING_ELEMENT, "lightning_element");
        processElement(entity, entityId, SpellAttachments.WATER_ELEMENT, "water_element");
        processElement(entity, entityId, SpellAttachments.NATURE_ELEMENT, "nature_element");
        processElement(entity, entityId, SpellAttachments.PROMOTION_ELEMENT, "promotion_element");
        if (event.getEntity().tickCount % 5 == 0){
            TickEvent.BurnTick(event);
        }

        if (event.getEntity().tickCount % 10 == 0) {
            TickEvent.ElectroReaction(event);
            TickEvent.VulnerabilityTick(event);
            CustomFreezeController.CheckFreezeStatus(event);
            TickEvent.FreezeElementTick(event);
            TickEvent.CheckDewSparkLayer(event);
        }

        if(event.getEntity().tickCount % 200 == 0){
            TickEvent.FreezeResistanceDecay(event);
        }
    }

    private static final Map<String, Boolean> elementZeroStateCache = new HashMap<>();

    private static final String CACHE_KEY_SEPARATOR = "|";

    private static void processElement(Entity entity, int entityId, Supplier<AttachmentType<ElementsAttachment>> elementKey, String elementName) {
        AttachmentType<ElementsAttachment> type = elementKey.get();
        if (type == null || !entity.hasData(type)) {
            return;
        }

        ElementsAttachment attachment = entity.getData(type);
        int duration = attachment.getValue();
        String cacheKey = entityId +
                CACHE_KEY_SEPARATOR +
                elementName;

        if (duration > 0) {
            entity.setData(type, new ElementsAttachment(duration - 1));
            elementZeroStateCache.remove(cacheKey);
        } else {
            if (attachment.getValue() != 0) {
                entity.setData(type, new ElementsAttachment(0));
            }
            if (elementZeroStateCache.putIfAbsent(cacheKey, Boolean.TRUE) == null) {
                PacketDistributor.sendToAllPlayers(new ElementData(entityId, elementName, 0));
            }
        }
    }
}
