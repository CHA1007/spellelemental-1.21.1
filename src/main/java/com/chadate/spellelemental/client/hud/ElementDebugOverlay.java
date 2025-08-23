package com.chadate.spellelemental.client.hud;


import com.chadate.spellelemental.client.network.custom.ClientPayloadHandler;
import com.chadate.spellelemental.network.ElementData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

public class ElementDebugOverlay {
	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Post event) {
		if (!ClientPayloadHandler.DebugState.enabled) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		Entity target = mc.crosshairPickEntity;
		if (target == null) return;

        PacketDistributor.sendToServer(new ElementData.ElementInspectRequest(target.getId()));

		if (ClientPayloadHandler.InspectCache.entityId != target.getId()) return;
		Map<String, Integer> snap = ClientPayloadHandler.InspectCache.data;
		if (snap.isEmpty()) return;

		GuiGraphics gg = event.getGuiGraphics();
		int x = 6, y = 6;
		gg.drawString(mc.font, "[Server Element Debug] EntityId: " + target.getId(), x, y, 0xFFFFFF, false);
		y += 10;
		for (Map.Entry<String, Integer> e : snap.entrySet()) {
			String line = e.getKey() + ": " + e.getValue();
			gg.drawString(mc.font, line, x, y, 0x7CFC00, false);
			y += 10;
		}
	}
} 