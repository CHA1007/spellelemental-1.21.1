package com.chadate.spellelemental.network;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.element.attachment.data.UnifiedElementAttachmentAssets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素附着资源同步数据包
 * 用于在玩家加入服务器时同步元素附着的视觉资源配置
 */
public record ElementAttachmentSyncPacket(
    Map<String, String> iconMap,
    Map<String, String> particleMap,
    Map<String, String> schoolMap
) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<ElementAttachmentSyncPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "element_attachment_sync"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ElementAttachmentSyncPacket> STREAM_CODEC = 
        StreamCodec.composite(
            StreamCodec.of(
                (buf, map) -> {
                    buf.writeInt(map.size());
                    map.forEach((key, value) -> {
                        buf.writeUtf(key);
                        buf.writeUtf(value);
                    });
                },
                buf -> {
                    int size = buf.readInt();
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        map.put(buf.readUtf(), buf.readUtf());
                    }
                    return map;
                }
            ), ElementAttachmentSyncPacket::iconMap,
            StreamCodec.of(
                (buf, map) -> {
                    buf.writeInt(map.size());
                    map.forEach((key, value) -> {
                        buf.writeUtf(key);
                        buf.writeUtf(value);
                    });
                },
                buf -> {
                    int size = buf.readInt();
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        map.put(buf.readUtf(), buf.readUtf());
                    }
                    return map;
                }
            ), ElementAttachmentSyncPacket::particleMap,
            StreamCodec.of(
                (buf, map) -> {
                    buf.writeInt(map.size());
                    map.forEach((key, value) -> {
                        buf.writeUtf(key);
                        buf.writeUtf(value);
                    });
                },
                buf -> {
                    int size = buf.readInt();
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        map.put(buf.readUtf(), buf.readUtf());
                    }
                    return map;
                }
            ), ElementAttachmentSyncPacket::schoolMap,
            ElementAttachmentSyncPacket::new
        );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 创建同步数据包，包含当前服务端的所有元素附着资源配置
     */
    public static ElementAttachmentSyncPacket create() {
        return new ElementAttachmentSyncPacket(
            UnifiedElementAttachmentAssets.getAllIcons(),
            UnifiedElementAttachmentAssets.getAllParticleEffects(),
            UnifiedElementAttachmentAssets.getAllSchools()
        );
    }
    
    /**
     * 客户端处理同步数据包
     */
    public static void handleClient(ElementAttachmentSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 清空客户端现有数据
            UnifiedElementAttachmentAssets.clear();
            
            // 同步服务端数据到客户端
            packet.iconMap.forEach(UnifiedElementAttachmentAssets::setIcon);
            packet.particleMap.forEach(UnifiedElementAttachmentAssets::setParticleEffect);
            packet.schoolMap.forEach(UnifiedElementAttachmentAssets::setSchool);
            
            SpellElemental.LOGGER.info("已同步 {} 个元素图标, {} 个粒子效果, {} 个学派配置", 
                packet.iconMap.size(), packet.particleMap.size(), packet.schoolMap.size());
        });
    }
}
