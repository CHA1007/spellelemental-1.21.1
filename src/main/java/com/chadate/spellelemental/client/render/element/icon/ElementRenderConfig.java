package com.chadate.spellelemental.client.render.element.icon;

public class ElementRenderConfig {
    public float spacing = 0.3f;
    public float maxDistanceSq = 400f;
    public float heightOffset = 0.5f;

    public static ElementRenderConfig getInstance() {
        return new ElementRenderConfig();
    }

}