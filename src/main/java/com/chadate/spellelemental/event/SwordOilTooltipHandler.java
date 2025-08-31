package com.chadate.spellelemental.event;

import com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 剑油物品提示事件处理器
 * 为所有配置为剑油类的物品自动添加元素类型和元素量提示
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class SwordOilTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查是否为剑油物品
        SwordOilConfigLoader.SwordOilConfig config = SwordOilConfigLoader.getSwordOilConfig(stack.getItem());
        if (config != null) {
            // 添加空行分隔
            event.getToolTip().add(Component.empty());
            
            // 添加剑油标题
            event.getToolTip().add(Component.literal("◆ ").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("tooltip.spellelemental.sword_oil.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            
            // 添加元素类型提示
            Component elementIcon = Component.literal(getElementIcon(config.getElement()) + " ")
                .withStyle(ChatFormatting.WHITE);
            Component elementName = Component.translatable("element.spellelemental." + config.getElement())
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            
            event.getToolTip().add(Component.literal("  ").append(elementIcon)
                .append(Component.translatable("tooltip.spellelemental.sword_oil.element", elementName)
                    .withStyle(ChatFormatting.GRAY)));
            
            // 添加元素量提示
            Component amountIcon = Component.literal("◆ ").withStyle(ChatFormatting.WHITE);
            Component amountValue = Component.literal(String.valueOf(config.getAmount()))
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            
            event.getToolTip().add(Component.literal("  ").append(amountIcon)
                .append(Component.translatable("tooltip.spellelemental.sword_oil.amount", amountValue)
                    .withStyle(ChatFormatting.GRAY)));
        }
    }
    

    
    /**
     * 根据元素类型获取对应的图标
     */
    private static String getElementIcon(String element) {
        return switch (element) {
            default -> "◆";
        };
    }
}
