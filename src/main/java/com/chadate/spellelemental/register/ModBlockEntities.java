package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.block.entity.SwordStandBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/**
 * 方块实体注册类
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SpellElemental.MODID);

    // 剑座方块实体
    public static final Supplier<BlockEntityType<SwordStandBlockEntity>> SWORD_STAND = 
        BLOCK_ENTITIES.register("sword_stand", () -> 
            BlockEntityType.Builder.of(SwordStandBlockEntity::new, ModBlocks.SWORD_STAND.get()).build(null)
        );

    /**
     * 注册方块实体到事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
