package com.chadate.spellelemental.data;

import com.chadate.spellelemental.SpellElemental;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SpellAttachments {

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SpellElemental.MODID);

	public static final Supplier<AttachmentType<ElementContainerAttachment>> ELEMENTS_CONTAINER = ATTACHMENT_TYPES.register(
			"elements_container", () -> AttachmentType.builder(ElementContainerAttachment::new).build()
	);

	public static void register(IEventBus eventBus) {
		ATTACHMENT_TYPES.register(eventBus);
	}
}
