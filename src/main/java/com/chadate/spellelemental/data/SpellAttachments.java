package com.chadate.spellelemental.data;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.data.custom.ElementsAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SpellAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SpellElemental.MODID);

    public static final Supplier<AttachmentType<ElementsAttachment>> FIRE_ELEMENT = ATTACHMENT_TYPES.register(
            "fire_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> ICE_ELEMENT = ATTACHMENT_TYPES.register(
            "ice_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> LIGHTNING_ELEMENT = ATTACHMENT_TYPES.register(
            "lightning_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> WATER_ELEMENT = ATTACHMENT_TYPES.register(
            "water_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> NATURE_ELEMENT = ATTACHMENT_TYPES.register(
            "nature_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> BURN_DAMAGE = ATTACHMENT_TYPES.register(
            "burn_damage", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> ELECTRO_DAMAGE = ATTACHMENT_TYPES.register(
            "electro_damage", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> VULNERABILITY_ELEMENT = ATTACHMENT_TYPES.register(
            "vulnerable_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> PROMOTION_ELEMENT = ATTACHMENT_TYPES.register(
            "promotion_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> DEWSPARK_TIME = ATTACHMENT_TYPES.register(
            "dewspark_time", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> DEWSPARK_LAYERS = ATTACHMENT_TYPES.register(
            "dewspark_layers", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> DEWSPARK_DAMAGE = ATTACHMENT_TYPES.register(
            "dewspark_damage", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> FREEZE_ELEMENT = ATTACHMENT_TYPES.register(
            "freeze_element", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );
    public static final Supplier<AttachmentType<ElementsAttachment>> FREEZE_LAYERS = ATTACHMENT_TYPES.register(
            "freeze_layers", () -> AttachmentType.builder(() -> new ElementsAttachment(0)).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
