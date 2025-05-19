package com.chadate.spellelemental;


public record ElementHandlerConfig(
        boolean enableFire,
        boolean enableIce,
        boolean enableLightning,
        boolean  enableNature

) {
    public static ElementHandlerConfig getDefault() {
        return new ElementHandlerConfig(true, true, true, true);
    }
}