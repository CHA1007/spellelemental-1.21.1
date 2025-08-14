package com.chadate.spellelemental.attribute;

import com.chadate.spellelemental.SpellElemental;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = SpellElemental.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, SpellElemental.MODID);

    public static final DeferredHolder<Attribute, Attribute> ASTRAL_BLESSING = ATTRIBUTES.register(
            "astral_blessing",
            () -> new RangedAttribute(
                    "attribute.spellelemental.astral_blessing",
                    0.0D,
                    0.0D,
                    10000.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> SPELL_CRIT_RATE = ATTRIBUTES.register(
            "spell_crit_rate",
            () -> new RangedAttribute(
                "attribute.spellelemental.spell_crit_rate",
                0.05D,
                -10000.0D,
                10000.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> SPELL_CRIT_DAMAGE = ATTRIBUTES.register(
            "spell_crit_damage",
            () -> new RangedAttribute(
                "attribute.spellelemental.spell_crit_damage",
                0.5D,
                0.0D,
                10000.0D
            ).setSyncable(true)
    );
    public static final DeferredHolder<Attribute, Attribute> PHYSICAL_DAMAGE_BOOST = ATTRIBUTES.register(
            "physical_damage_boost",
            () -> new RangedAttribute(
                "attribute.spellelemental.physical_damage_boost",
                1.0D,
                0.0D,
                10000.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> PHYSICAL_DAMAGE_RESIST = ATTRIBUTES.register(
            "physical_damage_resist",
            () -> new RangedAttribute(
                "attribute.spellelemental.physical_damage_resist",
                1.0D,
                -10000.0D,
                10000.0D
            ).setSyncable(true)
    );

    public static final DeferredHolder<Attribute, Attribute> HEALING_POWER = ATTRIBUTES.register(
            "healing_power",
            () -> new RangedAttribute(
                "attribute.spellelemental.healing_power",
                1.0D,
                0.0D,
                10000.0D
            ).setSyncable(true)
    );

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute)));
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

}
