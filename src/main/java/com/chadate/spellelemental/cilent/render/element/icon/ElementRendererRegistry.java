package com.chadate.spellelemental.cilent.render.element.icon;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class ElementRendererRegistry {
    private static final List<ElementRenderer> renderers = new ArrayList<>();

    public void register(ElementRenderer renderer) {
        renderers.add(renderer);
    }

    public static List<ElementRenderer> getActiveRenderers(LivingEntity entity) {
        return renderers.stream()
                .filter(r -> r.shouldRender(entity))
                .toList();
    }
}