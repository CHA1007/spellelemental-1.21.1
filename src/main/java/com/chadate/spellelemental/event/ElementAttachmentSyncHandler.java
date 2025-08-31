package com.chadate.spellelemental.event;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.network.ElementAttachmentSyncPacket;
import com.chadate.spellelemental.network.SwordOilConfigSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 元素附着同步事件处理器
 * 负责在玩家加入服务器时同步元素附着资源配置
 */
@EventBusSubscriber(modid = SpellElemental.MODID)
public class ElementAttachmentSyncHandler {
    
    /**
     * 当玩家登录服务器时，发送元素附着资源同步数据包和精油配置同步数据包
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 创建并发送元素附着资源同步数据包
            ElementAttachmentSyncPacket attachmentSyncPacket = ElementAttachmentSyncPacket.create();
            PacketDistributor.sendToPlayer(serverPlayer, attachmentSyncPacket);
            
            // 创建并发送精油配置同步数据包
            SwordOilConfigSyncPacket oilConfigSyncPacket = SwordOilConfigSyncPacket.create();
            PacketDistributor.sendToPlayer(serverPlayer, oilConfigSyncPacket);
            
            SpellElemental.LOGGER.debug("已向玩家 {} 发送元素附着资源和精油配置同步数据包", serverPlayer.getName().getString());
        }
    }
    
    /**
     * 当玩家切换维度时，重新发送同步数据包（可选，确保数据一致性）
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 创建并发送元素附着资源同步数据包
            ElementAttachmentSyncPacket attachmentSyncPacket = ElementAttachmentSyncPacket.create();
            PacketDistributor.sendToPlayer(serverPlayer, attachmentSyncPacket);
            
            // 创建并发送精油配置同步数据包
            SwordOilConfigSyncPacket oilConfigSyncPacket = SwordOilConfigSyncPacket.create();
            PacketDistributor.sendToPlayer(serverPlayer, oilConfigSyncPacket);
            
            SpellElemental.LOGGER.debug("已向切换维度的玩家 {} 发送元素附着资源和精油配置同步数据包", serverPlayer.getName().getString());
        }
    }
}
