package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 自定义创造模式物品栏注册类
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpellElemental.MODID);

    /**
     * 法术元素物品栏
     */
    public static final Supplier<CreativeModeTab> SPELL_ELEMENTAL_TAB = CREATIVE_MODE_TABS.register("spell_elemental", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.spellelemental"))
            .icon(() -> new ItemStack(ModItems.STELLAR_FRAGMENT.get()))
            .displayItems((parameters, output) -> {
                // 方块类物品
                output.accept(ModItems.SWORD_STAND.get());
                output.accept(ModItems.STELLAR_FRAGMENT_ORE.get());
                output.accept(ModItems.DEEPSLATE_STELLAR_FRAGMENT_ORE.get());
                
                // 材料物品
                output.accept(ModItems.STELLAR_FRAGMENT.get());
                
                // 精油物品
                output.accept(ModItems.FIRE_SWORD_OIL.get());
                output.accept(ModItems.ICE_SWORD_OIL.get());
                output.accept(ModItems.LIGHTNING_SWORD_OIL.get());
                output.accept(ModItems.BLOOD_SWORD_OIL.get());
                output.accept(ModItems.NATURE_SWORD_OIL.get());
            })
            .build()
    );

    /**
     * 注册创造物品栏到事件总线
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
