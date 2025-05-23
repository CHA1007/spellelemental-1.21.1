package com.chadate.spellelemental.element.attachment.attack;


public record ElementHandlerConfig(
        boolean enableFire,
        boolean enableIce,
        boolean enableLightning,
        boolean enableNature,
        boolean enableHoly,
        boolean enableEnder,
        boolean enableBlood

) {
    public static ElementHandlerConfig getDefault() {
        return new ElementHandlerConfig(
                true,
                true,
                true,
                true,
                true,
                true,
                true);
    }
}