package com.chadate.spellelemental.client.render;

import com.chadate.spellelemental.SpellElemental;
import com.chadate.spellelemental.client.network.custom.ClientPayloadHandler;
import com.chadate.spellelemental.element.attachment.data.UnifiedElementAttachmentAssets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = SpellElemental.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ElementAuraRenderer {
    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<LivingEntity, ?> event) {
        // 不在暂停时生成粒子（单人暂停或打开菜单时）
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;

        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (!(level instanceof ClientLevel)) return;
        Map<String, Integer> snapshot = entity.getData(com.chadate.spellelemental.data.SpellAttachments.ELEMENTS_CONTAINER).snapshot();
        if (snapshot.isEmpty()) return;

        // 控制粒子生成频率：每个元素每帧只有较小概率生成1个粒子
        for (String elementKey : snapshot.keySet()) {
            int remain = ClientPayloadHandler.DisplayCache.predictRemaining(entity.getId(), elementKey);
            if (remain == 0) continue; // 已过期

            String particleKey = UnifiedElementAttachmentAssets.getParticleEffect(elementKey);
            if (particleKey == null || particleKey.isEmpty()) continue;

            SimpleParticleType simple = resolveSimpleParticle(particleKey);
            if (simple == null) continue; // 仅支持无参数的简单粒子

            // 基础概率，剩余时间越少，概率略增（让将要结束时更明显）
            float baseChance = 0.15f; // 15% 概率/帧/元素
            float bonus = Math.max(0f, Math.min(0.15f, (ClientAuraConfig.FLASH_THRESHOLD - remain) / (float) ClientAuraConfig.FLASH_THRESHOLD * 0.15f));
            float chance = baseChance + bonus;
            if (RNG.nextFloat() > chance) continue;

            double radius = ClientAuraConfig.RADIUS;
            double angle = RNG.nextDouble() * Math.PI * 2.0;
            double yOff = 0.2 + RNG.nextDouble() * (entity.getBbHeight() * 0.8);
            double px = entity.getX() + Math.cos(angle) * radius;
            double py = entity.getY() + yOff;
            double pz = entity.getZ() + Math.sin(angle) * radius;

            double vx = (RNG.nextDouble() - 0.5) * 0.02;
            double vy = 0.02 + RNG.nextDouble() * 0.02;
            double vz = (RNG.nextDouble() - 0.5) * 0.02;

            level.addParticle(simple, px, py, pz, vx, vy, vz);
        }
    }

    private static SimpleParticleType resolveSimpleParticle(String key) {
        try {
            ResourceLocation rl = ResourceLocation.parse(key);
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(rl);
            if (type instanceof SimpleParticleType simple) {
                return simple;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // 简单的可调参数
    private static final class ClientAuraConfig {
        static final double RADIUS = 0.6; // 环绕半径
        static final int FLASH_THRESHOLD = 50; // 与图标闪烁阈值一致，便于联动
    }
}
