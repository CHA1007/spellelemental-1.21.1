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
    private final String elementType;
    private final int elementAmount;
    
    public SwordOilItem(Properties properties, String elementType, int elementAmount) {
        super(properties);
        this.elementType = elementType;
        this.elementAmount = elementAmount;
    }
    
    public String getElementType() {
        return elementType;
    }
    
    public int getElementAmount() {
        // 精油元素量由数据包 sword_oil_config.json 控制
        // 这里返回构造函数传入的默认值，实际使用时会从数据包获取
        return elementAmount;
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
