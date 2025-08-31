package com.chadate.spellelemental.enchantment;

import com.chadate.spellelemental.register.ModAttributes;
import com.chadate.spellelemental.register.ModEnchantments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

/**
 * 星耀祝福附魔效果处理器
 * 负责计算和应用星耀祝福附魔的属性加成
 * 每级附魔提供20点星耀祝福属性，四件盔甲可以叠加
 */
@EventBusSubscriber(modid = "spellelemental")
public class AstralBlessingEnchantmentHandler {
    
    // 每级附魔提供的星耀祝福数值
    private static final double ASTRAL_BLESSING_PER_LEVEL = 20.0;
    
    // 属性修饰符的唯一ID，用于识别和移除
    private static final ResourceLocation ASTRAL_BLESSING_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("spellelemental", "astral_blessing_enchantment");
    
    /**
     * 监听装备变化事件，当玩家装备或卸下盔甲时重新计算星耀祝福属性
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 只处理盔甲槽位的变化
        EquipmentSlot slot = event.getSlot();
        if (isArmorSlot(slot)) {
            updateAstralBlessingAttribute(entity);
        }
    }
    
    /**
     * 更新实体的星耀祝福属性
     * 计算所有盔甲装备上的星耀祝福附魔等级总和
     */
    public static void updateAstralBlessingAttribute(LivingEntity entity) {
        AttributeInstance attributeInstance = entity.getAttribute(ModAttributes.ASTRAL_BLESSING);
        if (attributeInstance == null) {
            return;
        }
        
        // 移除之前的附魔属性修饰符
        attributeInstance.removeModifier(ASTRAL_BLESSING_MODIFIER_ID);
        
        // 计算所有盔甲装备的星耀祝福附魔等级总和
        int totalEnchantmentLevel = calculateTotalAstralBlessingLevel(entity);
        
        // 如果有星耀祝福附魔，添加属性修饰符
        if (totalEnchantmentLevel > 0) {
            double bonusValue = totalEnchantmentLevel * ASTRAL_BLESSING_PER_LEVEL;
            AttributeModifier modifier = new AttributeModifier(
                ASTRAL_BLESSING_MODIFIER_ID,
                bonusValue,
                AttributeModifier.Operation.ADD_VALUE
            );
            attributeInstance.addPermanentModifier(modifier);
        }
    }
    
    /**
     * 计算实体所有盔甲装备上的星耀祝福附魔等级总和
     */
    private static int calculateTotalAstralBlessingLevel(LivingEntity entity) {
        int totalLevel = 0;
        
        // 检查所有盔甲槽位
        for (EquipmentSlot slot : getArmorSlots()) {
            ItemStack armorStack = entity.getItemBySlot(slot);
            if (!armorStack.isEmpty()) {
                // 使用注册表获取附魔 Holder
                var enchantmentRegistry = entity.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                var enchantmentHolder = enchantmentRegistry.getHolder(ModEnchantments.ASTRAL_BLESSING);
                if (enchantmentHolder.isPresent()) {
                    int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantmentHolder.get(), armorStack);
                    totalLevel += enchantmentLevel;
                }
            }
        }
        
        return totalLevel;
    }
    
    /**
     * 判断是否为盔甲槽位
     */
    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || 
               slot == EquipmentSlot.CHEST || 
               slot == EquipmentSlot.LEGS || 
               slot == EquipmentSlot.FEET;
    }
    
    /**
     * 获取所有盔甲槽位
     */
    private static EquipmentSlot[] getArmorSlots() {
        return new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };
    }
    
    /**
     * 获取星耀祝福附魔的每级数值
     */
    public static double getAstralBlessingPerLevel() {
        return ASTRAL_BLESSING_PER_LEVEL;
    }
}
