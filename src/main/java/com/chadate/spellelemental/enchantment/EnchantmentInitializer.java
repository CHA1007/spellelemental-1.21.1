package com.chadate.spellelemental.enchantment;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 附魔初始化处理器
 * 负责在实体加入世界或玩家登录时初始化附魔效果
 */
@EventBusSubscriber(modid = "spellelemental")
public class EnchantmentInitializer {
    
    /**
     * 当玩家登录时初始化附魔效果
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        // 初始化星耀祝福附魔效果
        AstralBlessingEnchantmentHandler.updateAstralBlessingAttribute(player);
    }
    
    /**
     * 当玩家重生时重新初始化附魔效果
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        // 重新初始化星耀祝福附魔效果
        AstralBlessingEnchantmentHandler.updateAstralBlessingAttribute(player);
    }
    
    /**
     * 当实体加入世界时初始化附魔效果（用于非玩家实体）
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
            // 为非玩家生物初始化星耀祝福附魔效果
            AstralBlessingEnchantmentHandler.updateAstralBlessingAttribute(livingEntity);
        }
    }
}
