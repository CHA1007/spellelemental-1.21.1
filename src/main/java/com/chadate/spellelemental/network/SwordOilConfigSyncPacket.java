package com.chadate.spellelemental.network;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 精油配置同步数据包
 * 用于在玩家加入服务器时同步精油配置数据到客户端
 * 确保客户端的JEI、物品提示等功能能够正确显示精油信息
 */
public record SwordOilConfigSyncPacket(
    List<SwordOilConfigData> configs
) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SwordOilConfigSyncPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID, "sword_oil_config_sync"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SwordOilConfigSyncPacket> STREAM_CODEC = 
        StreamCodec.composite(
            StreamCodec.of(
                (buf, configs) -> {
                    buf.writeInt(configs.size());
                    for (SwordOilConfigData config : configs) {
                        buf.writeUtf(config.itemId());
                        buf.writeUtf(config.element());
                        buf.writeInt(config.amount());
                        buf.writeUtf(config.displayName());
                    }
                },
                buf -> {
                    int size = buf.readInt();
                    List<SwordOilConfigData> configs = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        configs.add(new SwordOilConfigData(
                            buf.readUtf(),
                            buf.readUtf(),
                            buf.readInt(),
                            buf.readUtf()
                        ));
                    }
                    return configs;
                }
            ), SwordOilConfigSyncPacket::configs,
            SwordOilConfigSyncPacket::new
        );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 创建同步数据包，包含当前服务端的所有精油配置
     */
    public static SwordOilConfigSyncPacket create() {
        List<SwordOilConfigData> configData = new ArrayList<>();
        
        // 从服务端的配置加载器获取所有配置
        for (SwordOilConfigLoader.SwordOilConfig config : SwordOilConfigLoader.getSwordOilConfigs()) {
            configData.add(new SwordOilConfigData(
                config.getItemId(),
                config.getElement(),
                config.getAmount(),
                config.getDisplayName()
            ));
        }
        
        return new SwordOilConfigSyncPacket(configData);
    }
    
    /**
     * 客户端处理同步数据包
     */
    public static void handleClient(SwordOilConfigSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 清空客户端现有配置
            SwordOilConfigLoader.clearConfigs();
            
            // 同步服务端配置到客户端
            for (SwordOilConfigData configData : packet.configs) {
                SwordOilConfigLoader.addConfig(new SwordOilConfigLoader.SwordOilConfig(
                    configData.itemId(),
                    configData.element(),
                    configData.amount(),
                    configData.displayName()
                ));
            }
            
            SpellElemental.LOGGER.info("已同步 {} 个精油配置到客户端", packet.configs.size());
        });
    }
    
    /**
     * 精油配置数据传输对象
     */
    public record SwordOilConfigData(
        String itemId,
        String element,
        int amount,
        String displayName
    ) {}
}
