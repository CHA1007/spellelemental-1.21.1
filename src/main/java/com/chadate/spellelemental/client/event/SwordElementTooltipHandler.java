package com.chadate.spellelemental.client.event;

import com.chadate.spellelemental.config.ServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 剑元素附着工具提示处理器
 * 在剑的工具提示中显示元素附着信息
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class SwordElementTooltipHandler {
    
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        
        // 检查是否为剑类武器
        if (!(stack.getItem() instanceof SwordItem)) {
            return;
        }
        
        // 检查剑是否有元素附着
        CompoundTag customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = customData.getCompound("ElementAttachment");
        
        if (!elementData.contains("element") || !elementData.contains("amount")) {
            return;
        }
        
        String elementType = elementData.getString("element");
        int elementAmount = elementData.getInt("amount");
        
        // 获取武器最大元素量
        int maxElementAmount = ServerConfig.getWeaponMaxElementAmount();
        
        // 添加元素附着信息到工具提示
        event.getToolTip().add(Component.literal(""));  // 空行分隔
        
        // 元素附着 - 元素名称用金色突出显示
        Component elementName = Component.translatable("element.spellelemental." + elementType)
            .withStyle(style -> style.withColor(0xFFD700));  // 金色突出显示
        event.getToolTip().add(Component.translatable("tooltip.spellelemental.sword.element_attachment", elementName)
            .withStyle(style -> style.withColor(0xFFFFFF)));  // 白色
        
        // 元素量 - 显示当前量/最大量格式，数值用黄色突出显示
        Component amountValue = Component.literal(elementAmount + "/" + maxElementAmount)
            .withStyle(style -> style.withColor(0xFFFF55));  // 黄色数值突出显示
        event.getToolTip().add(Component.translatable("tooltip.spellelemental.sword.element_amount", amountValue)
            .withStyle(style -> style.withColor(0x888888)));  // 灰色标签
    }
    

}
