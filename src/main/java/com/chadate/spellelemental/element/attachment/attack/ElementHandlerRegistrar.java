package com.chadate.spellelemental.element.attachment.attack;

import com.chadate.spellelemental.element.attachment.attack.custom.FireElementHandler;
import com.chadate.spellelemental.element.attachment.attack.custom.IceElementHandler;
import com.chadate.spellelemental.element.attachment.attack.custom.LightningElementHandler;
import com.chadate.spellelemental.element.attachment.attack.custom.NatureElementHandler;

public class ElementHandlerRegistrar {
    public static void registerDefaultHandlers() {
        ElementAttachmentRegistry.register(new FireElementHandler());
        ElementAttachmentRegistry.register(new IceElementHandler());
        ElementAttachmentRegistry.register(new LightningElementHandler());
        ElementAttachmentRegistry.register(new NatureElementHandler());
    }

    public static void registerWithConfiguration(ElementHandlerConfig config) {
        if (config.enableFire()) {
            ElementAttachmentRegistry.register(new FireElementHandler());
        }
        if (config.enableIce()) {
            ElementAttachmentRegistry.register(new IceElementHandler());
        }
        if (config.enableLightning()){
            ElementAttachmentRegistry.register(new LightningElementHandler());
        }
        if (config.enableNature()) {
            ElementAttachmentRegistry.register(new NatureElementHandler());
        }
    }
}