package com.chadate.spellelemental.element.attachment.environmental.handler;

import com.chadate.spellelemental.element.attachment.environmental.config.EnvironmentalAttachmentConfig;
import com.chadate.spellelemental.data.ElementsAttachment;
import com.chadate.spellelemental.client.network.custom.ElementData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 动态环境条件元素附着处理器
 * 根据JSON配置处理环境条件触发的元素附着
 */
public class DynamicEnvironmentalHandler implements EnvironmentalAttachmentHandler {
    
    private final EnvironmentalAttachmentConfig config;
    private final Lazy<AttachmentType<ElementsAttachment>> attachmentType;
    
    public DynamicEnvironmentalHandler(EnvironmentalAttachmentConfig config) {
        this.config = config;
        this.attachmentType = Lazy.of(() -> getAttachmentTypeFromString(config.attachmentType));
    }
    
    @Override
    public boolean shouldApply(LivingEntity entity) {
        if (config.environmentalConditions == null) {
            return false;
        }
        
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        
        // 检查水条件
        if (config.environmentalConditions.waterConditions != null) {
            if (!checkWaterConditions(entity, config.environmentalConditions.waterConditions)) {
                return false;
            }
        }
        
        // 检查天气条件
        if (config.environmentalConditions.weatherConditions != null) {
            if (!checkWeatherConditions(level, pos, config.environmentalConditions.weatherConditions)) {
                return false;
            }
        }
        
        // 检查生物群系条件
        if (config.environmentalConditions.biomeConditions != null) {
            if (!checkBiomeConditions(level, pos, config.environmentalConditions.biomeConditions)) {
                return false;
            }
        }
        
        // 检查方块条件
        if (config.environmentalConditions.blockConditions != null) {
            if (!checkBlockConditions(level, pos, config.environmentalConditions.blockConditions)) {
                return false;
            }
        }
        
        // 检查时间条件
        if (config.environmentalConditions.timeConditions != null) {
            if (!checkTimeConditions(level, config.environmentalConditions.timeConditions)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void applyEffect(LivingEntity entity) {
        AttachmentType<ElementsAttachment> type = attachmentType.get();
        if (type == null) return;
        
        int duration = config.effects != null ? config.effects.duration : 200;
        
        // 如果配置了刷新触发，则重置持续时间；否则只在没有效果时应用
        if (config.effects != null && config.effects.refreshOnTrigger) {
            entity.getData(type).setValue(duration);
        } else {
            // 只有当前没有效果时才应用
            if (entity.getData(type).getValue() <= 0) {
                entity.getData(type).setValue(duration);
            }
        }
        
        // 网络同步
        if (config.effects != null && config.effects.networkSync) {
            PacketDistributor.sendToAllPlayers(new ElementData(
                entity.getId(), 
                config.elementId + "_element", 
                entity.getData(type).getValue()
            ));
        }
    }
    
    @Override
    public int getCheckInterval() {
        return config.environmentalConditions != null ? 
            config.environmentalConditions.checkInterval : 20;
    }
    
    @Override
    public String getElementId() {
        return config.elementId;
    }
    
    private boolean checkWaterConditions(LivingEntity entity, EnvironmentalAttachmentConfig.WaterConditions conditions) {
        if (conditions.inWater && entity.isInWater()) {
            return true;
        }
        if (conditions.inWaterOrRain && entity.isInWaterRainOrBubble()) {
            return true;
        }
        if (conditions.touchingWater && entity.isInWaterOrBubble()) {
            return true;
        }
        return false;
    }
    
    private boolean checkWeatherConditions(Level level, BlockPos pos, EnvironmentalAttachmentConfig.WeatherConditions conditions) {
        if (conditions.raining && level.isRainingAt(pos)) {
            return true;
        }
        if (conditions.thundering && level.isThundering()) {
            return true;
        }
        if (conditions.clearSky && !level.isRaining() && !level.isThundering()) {
            return true;
        }
        return false;
    }
    
    private boolean checkBiomeConditions(Level level, BlockPos pos, EnvironmentalAttachmentConfig.BiomeConditions conditions) {
        Biome biome = level.getBiome(pos).value();
        
        // 检查生物群系标签
        if (conditions.biomeTags != null && !conditions.biomeTags.isEmpty()) {
            for (String tagName : conditions.biomeTags) {
                try {
                    ResourceLocation tagLocation = ResourceLocation.parse(tagName);
                    TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagLocation);
                    if (level.getBiome(pos).is(tag)) {
                        return true;
                    }
                } catch (Exception e) {
                    // 如果解析标签失败，跳过这个标签
                    continue;
                }
            }
        }
        
        // 检查生物群系ID
        if (conditions.biomeIds != null && !conditions.biomeIds.isEmpty()) {
            ResourceLocation biomeId = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
            if (biomeId != null && conditions.biomeIds.contains(biomeId.toString())) {
                return true;
            }
        }
        
        // 检查温度范围
        if (conditions.temperatureRange != null) {
            float temperature = biome.getBaseTemperature();
            if (temperature >= conditions.temperatureRange.minTemperature && 
                temperature <= conditions.temperatureRange.maxTemperature) {
                return true;
            }
        }
        
        return conditions.biomeTags == null && conditions.biomeIds == null && conditions.temperatureRange == null;
    }
    
    private boolean checkBlockConditions(Level level, BlockPos pos, EnvironmentalAttachmentConfig.BlockConditions conditions) {
        // 检查脚下方块
        if (conditions.standingOnBlocks != null && !conditions.standingOnBlocks.isEmpty()) {
            Block blockBelow = level.getBlockState(pos.below()).getBlock();
            ResourceLocation blockId = level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(blockBelow);
            if (blockId != null && conditions.standingOnBlocks.contains(blockId.toString())) {
                return true;
            }
        }
        
        // 检查附近方块
        if (conditions.nearbyBlocks != null && !conditions.nearbyBlocks.isEmpty()) {
            int radius = conditions.searchRadius;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        Block block = level.getBlockState(checkPos).getBlock();
                        ResourceLocation blockId = level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(block);
                        if (blockId != null && conditions.nearbyBlocks.contains(blockId.toString())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return conditions.standingOnBlocks == null && conditions.nearbyBlocks == null;
    }
    
    private boolean checkTimeConditions(Level level, EnvironmentalAttachmentConfig.TimeConditions conditions) {
        long dayTime = level.getDayTime() % 24000;
        
        if (conditions.dayTime && dayTime >= 0 && dayTime < 12000) {
            return true;
        }
        if (conditions.nightTime && (dayTime >= 12000 && dayTime < 24000)) {
            return true;
        }
        
        if (conditions.timeRange != null) {
            long start = conditions.timeRange.startTime;
            long end = conditions.timeRange.endTime;
            
            if (start <= end) {
                // 正常时间范围
                return dayTime >= start && dayTime <= end;
            } else {
                // 跨越午夜的时间范围
                return dayTime >= start || dayTime <= end;
            }
        }
        
        return !conditions.dayTime && !conditions.nightTime && conditions.timeRange == null;
    }
    
    @SuppressWarnings("unchecked")
    private AttachmentType<ElementsAttachment> getAttachmentTypeFromString(String attachmentTypeString) {
        // 这里需要根据字符串获取对应的AttachmentType
        // 这个实现需要根据你的SpellAttachments类的具体实现来调整
        try {
            String fieldName = attachmentTypeString.substring(attachmentTypeString.lastIndexOf(':') + 1).toUpperCase();
            java.lang.reflect.Field field = com.chadate.spellelemental.data.SpellAttachments.class.getField(fieldName);
            return (AttachmentType<ElementsAttachment>) ((Supplier<?>) field.get(null)).get();
        } catch (Exception e) {
            System.err.println("Failed to get attachment type for: " + attachmentTypeString);
            return null;
        }
    }
}
