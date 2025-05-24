package com.chadate.spellelemental.element.attachment.attack;

import com.chadate.spellelemental.element.attachment.attack.custom.*;

public class ElementHandlerRegistrar {
    public static void registerDefaultHandlers() {
        ElementAttachmentRegistry.register(new FireElementHandler());
        ElementAttachmentRegistry.register(new IceElementHandler());
        ElementAttachmentRegistry.register(new LightningElementHandler());
        ElementAttachmentRegistry.register(new NatureElementHandler());
        ElementAttachmentRegistry.register(new HolyElementHander());
        ElementAttachmentRegistry.register(new EnderElementHadler());
        ElementAttachmentRegistry.register(new BloodElementHandler());
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
        if (config.enableHoly()) {
            ElementAttachmentRegistry.register(new HolyElementHander());
        }
        if (config.enableEnder()) {
            ElementAttachmentRegistry.register(new EnderElementHadler());
        }
        if (config.enableBlood()) {
            ElementAttachmentRegistry.register(new BloodElementHandler());
        }
    }
}