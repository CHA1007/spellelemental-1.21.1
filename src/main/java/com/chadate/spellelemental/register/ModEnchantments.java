package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> ASTRAL_BLESSING = key("astral_blessing");
    public static final ResourceKey<Enchantment> FLAME_AFFIX = key("flame_affix");

    public static void bootstrap(BootstrapContext<Enchantment> context)
    {
        // 获取各种注册表的持有者获取器
        HolderGetter<DamageType> holdergetter = context.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> holdergetter1 = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> holdergetter2 = context.lookup(Registries.ITEM);
        HolderGetter<Block> holdergetter3 = context.lookup(Registries.BLOCK);

        register(
                context,
                ASTRAL_BLESSING,
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                5,
                                4,
                                Enchantment.dynamicCost(15, 5),
                                Enchantment.dynamicCost(100, 5),
                                2,
                                EquipmentSlotGroup.ARMOR
                        )
                )
        );

        register(
                context,
                FLAME_AFFIX,
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                                5,
                                4,
                                Enchantment.dynamicCost(15, 5),
                                Enchantment.dynamicCost(100, 5),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
    }

    // 注册附魔的方法
    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    // 创建附魔资源键的方法
    private static ResourceKey<Enchantment> key(String name)
    {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(SpellElemental.MODID,name));
    }
}