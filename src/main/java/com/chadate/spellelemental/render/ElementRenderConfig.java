package com.chadate.spellelemental.render;

public class ElementRenderConfig {
    public float baseScale = 0.1f;
    public float spacing = 0.3f;
    public float maxDistanceSq = 400f;
    public float heightOffset = 0.5f;

    public static ElementRenderConfig getInstance() {
        return new ElementRenderConfig();
    }

    private ElementRenderConfig getConfig() {
        return new ElementRenderConfig();
    }
}