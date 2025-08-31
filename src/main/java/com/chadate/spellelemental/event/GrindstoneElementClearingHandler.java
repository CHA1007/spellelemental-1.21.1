package com.chadate.spellelemental.event;

import com.chadate.spellelemental.util.ActionBarMessageUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 处理玩家手持剑Shift右键砂轮清除元素的事件
 */
@EventBusSubscriber(modid = "spellelemental")
public class GrindstoneElementClearingHandler {

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        // 检查是否为手持剑 + Shift + 右键砂轮
        if (!(heldItem.getItem() instanceof SwordItem) || 
            !player.isShiftKeyDown() || 
            event.getLevel().getBlockState(event.getPos()).getBlock() != Blocks.GRINDSTONE) {
            return;
        }
        
        // 防止在客户端执行
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        // 检查剑是否有元素附着
        CompoundTag tag = heldItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = tag.getCompound("ElementAttachment");
        
        if (elementData.contains("element")) {
            // 如果有元素附着，清除元素
            String elementType = elementData.getString("element");
            int elementAmount = elementData.getInt("amount");
            
            // 清除元素数据
            tag.remove("ElementAttachment");
            heldItem.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                net.minecraft.world.item.component.CustomData.of(tag));
            
            // 提示玩家元素已清除（使用动作栏消息）
            if (player instanceof ServerPlayer serverPlayer) {
                ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                    "message.spellelemental.sword_oil.element_cleared", 
                    Component.translatable("element.spellelemental." + elementType), elementAmount);
            }
            
            // 播放砂轮使用音效
            event.getLevel().playSound(null, event.getPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // 取消原本的砂轮交互
            event.setCanceled(true);
        } else {
            // 如果没有元素附着，提示玩家（使用动作栏消息）
            if (player instanceof ServerPlayer serverPlayer) {
                ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                    "message.spellelemental.sword_oil.no_element_to_clear");
            }
            event.setCanceled(true);
        }
    }
}
