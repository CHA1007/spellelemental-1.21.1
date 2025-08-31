package com.chadate.spellelemental.register;


import com.chadate.spellelemental.SpellElemental;
import io.redspace.ironsspellbooks.fluids.NoopFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluid {
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, SpellElemental.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, SpellElemental.MODID);
    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }

    public static final DeferredHolder<FluidType, FluidType> STELLAR_ESSENCE_TYPE = FLUID_TYPES.register("stellar_essence", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> FIRE_SWORD_OIL_TYPE = FLUID_TYPES.register("fire_sword_oil", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> ICE_SWORD_OIL_TYPE = FLUID_TYPES.register("ice_sword_oil", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> LIGHTNING_SWORD_OIL_TYPE = FLUID_TYPES.register("lightning_sword_oil", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> BLOOD_SWORD_OIL_TYPE = FLUID_TYPES.register("blood_sword_oil", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> NATURE_TYPE = FLUID_TYPES.register("nature_sword_oil", () -> new FluidType(FluidType.Properties.create()));

    public static final DeferredHolder<Fluid, NoopFluid> STELLAR_ESSENCE = registerNoop("stellar_essence", STELLAR_ESSENCE_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> FIRE_SWORD_OIL = registerNoop("fire_sword_oil", FIRE_SWORD_OIL_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> ICE_SWORD_OIL = registerNoop("ice_sword_oil", ICE_SWORD_OIL_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> LIGHTNING_SWORD_OIL = registerNoop("lightning_sword_oil", LIGHTNING_SWORD_OIL_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> BLOOD_SWORD_OIL = registerNoop("blood_sword_oil", BLOOD_SWORD_OIL_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> NATURE_SWORD_OIL = registerNoop("nature_sword_oil", NATURE_TYPE::value);

    private static DeferredHolder<Fluid, NoopFluid> registerNoop(String name, Supplier<FluidType> fluidType) {
        DeferredHolder<Fluid, NoopFluid> holder = DeferredHolder.create(Registries.FLUID, SpellElemental.id(name));
        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidType, holder::value, holder::value).bucket(() -> Items.AIR);
        FLUIDS.register(name, () -> new NoopFluid(properties));
        return holder;
    }
}
