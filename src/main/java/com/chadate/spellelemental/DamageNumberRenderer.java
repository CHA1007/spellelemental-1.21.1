package com.chadate.spellelemental;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;

public class DamageNumberRenderer {
    private static final DamageNumberRenderer INSTANCE = new DamageNumberRenderer();
    private static final Map<Entity, Integer> damageNumbers = new HashMap<>();

    public static DamageNumberRenderer getInstance() {
        return INSTANCE;
    }

    public void addDamageNumber(Entity entity, int damage) {
        damageNumbers.put(entity, damage);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        for (Map.Entry<Entity, Integer> entry : damageNumbers.entrySet()) {
            Entity entity = entry.getKey();
            int damage = entry.getValue();

            Vec3 pos = entity.getPosition(event.getPartialTick().getRealtimeDeltaTicks());
            String text = String.valueOf(damage);

            // 将3D坐标转换为2D屏幕坐标
            Vec3 screenPos = mc.gameRenderer.getMainCamera().getPosition().subtract(pos).normalize();
            int x = (int) ((double) mc.getWindow().getGuiScaledWidth() / 2 + screenPos.x * 100);
            int y = (int) ((double) mc.getWindow().getGuiScaledHeight() / 2 - screenPos.y * 100);

            guiGraphics.drawString(font, text, x, y, 0xFFFFFF);
        }

        damageNumbers.clear();
    }
}
