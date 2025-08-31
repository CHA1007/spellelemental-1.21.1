package com.chadate.spellelemental.register;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.block.SwordStandBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 方块注册类
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, SpellElemental.MODID);

    // 剑座方块
    public static final Supplier<Block> SWORD_STAND = BLOCKS.register("sword_stand", 
        () -> new SwordStandBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK)
            .strength(2.0F)
            .sound(SoundType.CHERRY_WOOD)
            .noOcclusion()
        )
    );

    // 星辰矿石
    public static final Supplier<Block> STELLAR_FRAGMENT_ORE = BLOCKS.register("stellar_fragment_ore",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(4.0F, 3.0F)
            .sound(SoundType.STONE)
        )
    );

    // 深层星辰矿石
    public static final Supplier<Block> DEEPSLATE_STELLAR_FRAGMENT_ORE = BLOCKS.register("deepslate_stellar_fragment_ore",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.DEEPSLATE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5.5F, 4.0F)
            .sound(SoundType.DEEPSLATE)
        )
    );

    /**
     * 注册方块到事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
