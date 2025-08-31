package com.chadate.spellelemental.integration.jei.recipe;

import net.minecraft.world.item.ItemStack;

/**
 * 剑油应用配方记录
 * 表示使用剑油为剑附着元素的过程
 */
public record SwordOilApplicationRecipe(
    ItemStack inputSword,      // 输入的剑
    ItemStack swordOil,        // 使用的剑油
    ItemStack swordStand,      // 需要的剑座
    ItemStack outputSword,     // 输出的附着元素的剑
    String elementType,        // 元素类型
    int elementAmount          // 元素量
) {
    
    /**
     * 获取输入的剑
     */
    public ItemStack getInputSword() {
        return inputSword;
    }
    
    /**
     * 获取使用的剑油
     */
    public ItemStack getSwordOil() {
        return swordOil;
    }
    
    /**
     * 获取需要的剑座
     */
    public ItemStack getSwordStand() {
        return swordStand;
    }
    
    /**
     * 获取输出的附着元素的剑
     */
    public ItemStack getOutputSword() {
        return outputSword;
    }
    
    /**
     * 获取元素类型
     */
    public String getElementType() {
        return elementType;
    }
    
    /**
     * 获取元素量
     */
    public int getElementAmount() {
        return elementAmount;
    }
}
