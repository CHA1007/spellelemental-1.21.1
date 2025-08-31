package com.chadate.spellelemental.util;

import com.chadate.spellelemental.network.ActionBarMessagePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 动作栏消息工具类
 */
public class ActionBarMessageUtil {
    
    /**
     * 向指定玩家发送动作栏消息
     */
    public static void sendActionBarMessage(ServerPlayer player, Component message) {
        PacketDistributor.sendToPlayer(player, new ActionBarMessagePacket(message));
    }
    
    /**
     * 向指定玩家发送动作栏消息（字符串版本）
     */
    public static void sendActionBarMessage(ServerPlayer player, String message) {
        sendActionBarMessage(player, Component.literal(message));
    }
    
    /**
     * 向指定玩家发送可翻译的动作栏消息
     */
    public static void sendTranslatableActionBarMessage(ServerPlayer player, String key, Object... args) {
        sendActionBarMessage(player, Component.translatable(key, args));
    }
}
