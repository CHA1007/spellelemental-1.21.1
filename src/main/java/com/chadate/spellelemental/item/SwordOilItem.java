package com.chadate.spellelemental.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 剑油物品基类
 */
public class SwordOilItem extends Item {

    public SwordOilItem(Properties properties) {
        super(properties);
    }

    public String getElementType() {
        // 元素类型完全由数据包 sword_oil_config.json 控制
        // 从配置加载器中动态获取当前物品的元素类型
        var config = com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader.getSwordOilConfig(this);
        return config != null ? config.getElement() : ""; // 如果配置不存在，返回空字符串
    }
    
    public int getElementAmount() {
        // 精油元素量完全由数据包 sword_oil_config.json 控制
        // 从配置加载器中动态获取当前物品的元素量
        var config = com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader.getSwordOilConfig(this);
        return config != null ? config.getAmount() : 0; // 如果配置不存在，返回0
    }
    
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
    
    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        // 让剑油物品有附魔光效
        return true;
    }
}
