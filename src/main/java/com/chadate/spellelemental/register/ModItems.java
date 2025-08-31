package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;

import com.chadate.spellelemental.item.SwordOilItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 物品注册类
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SpellElemental.MODID);

    public static final Supplier<Item> SWORD_STAND = ITEMS.register("sword_stand",
        () -> new BlockItem(ModBlocks.SWORD_STAND.get(), new Item.Properties().rarity(Rarity.COMMON))
    );

    public static final Supplier<Item> FIRE_SWORD_OIL = ITEMS.register("fire_sword_oil",
        () -> new SwordOilItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "fire", 3000)
    );

    public static final Supplier<Item> ICE_SWORD_OIL = ITEMS.register("ice_sword_oil",
        () -> new SwordOilItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "ice", 3000)
    );

    public static final Supplier<Item> LIGHTNING_SWORD_OIL = ITEMS.register("lightning_sword_oil",
        () -> new SwordOilItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "lightning", 3000)
    );

    public static final Supplier<Item> BLOOD_SWORD_OIL = ITEMS.register("blood_sword_oil",
        () -> new SwordOilItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "blood", 3000)
    );

    public static final Supplier<Item> NATURE_SWORD_OIL = ITEMS.register("nature_sword_oil",
        () -> new SwordOilItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "nature", 3000)
    );

    public static final Supplier<Item> STELLAR_FRAGMENT = ITEMS.register("stellar_fragment",
        () -> new Item(new Item.Properties().rarity(Rarity.RARE))
    );

    public static final Supplier<Item> STELLAR_FRAGMENT_ORE = ITEMS.register("stellar_fragment_ore",
        () -> new BlockItem(ModBlocks.STELLAR_FRAGMENT_ORE.get(), new Item.Properties().rarity(Rarity.COMMON))
    );

    public static final Supplier<Item> DEEPSLATE_STELLAR_FRAGMENT_ORE = ITEMS.register("deepslate_stellar_fragment_ore",
        () -> new BlockItem(ModBlocks.DEEPSLATE_STELLAR_FRAGMENT_ORE.get(), new Item.Properties().rarity(Rarity.COMMON))
    );

    /**
     * 注册物品到事件总线
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
