package com.chadate.spellelemental.event;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.register.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * 创造模式物品栏事件处理器
 * 负责将模组物品添加到正确的创造模式物品栏中
 */
@EventBusSubscriber(modid = SpellElemental.MODID)
public class CreativeModeTabsHandler {

    /**
     * 添加物品到创造模式物品栏
     * 物品同时存在于自定义物品栏和原版物品栏中，方便玩家查找
     */
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(new ItemStack(ModItems.SWORD_STAND.get()));
        }
        
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(new ItemStack(ModItems.STELLAR_FRAGMENT_ORE.get()));
            event.accept(new ItemStack(ModItems.DEEPSLATE_STELLAR_FRAGMENT_ORE.get()));
        }
        
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(ModItems.STELLAR_FRAGMENT.get()));
            event.accept(new ItemStack(ModItems.FIRE_SWORD_OIL.get()));
            event.accept(new ItemStack(ModItems.ICE_SWORD_OIL.get()));
            event.accept(new ItemStack(ModItems.LIGHTNING_SWORD_OIL.get()));
            event.accept(new ItemStack(ModItems.BLOOD_SWORD_OIL.get()));
            event.accept(new ItemStack(ModItems.NATURE_SWORD_OIL.get()));
        }
    }
}
