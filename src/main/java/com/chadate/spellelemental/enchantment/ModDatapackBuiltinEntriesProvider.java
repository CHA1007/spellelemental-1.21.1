package com.chadate.spellelemental.enchantment;


import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {
    // 定义一个RegistrySetBuilder实例，用于注册附魔相关的条目
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap);

    // 构造函数，初始化ModDatapackBuiltinEntriesProvider实例
    public ModDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SpellElemental.MODID));
    }
}