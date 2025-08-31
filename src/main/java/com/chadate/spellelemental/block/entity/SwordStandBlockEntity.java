package com.chadate.spellelemental.block.entity;

import com.chadate.spellelemental.register.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 剑座方块实体类
 * 负责存储和管理剑座中的剑数据
 */
public class SwordStandBlockEntity extends BlockEntity implements WorldlyContainer {
    // 单槽容器，用于与漏斗/管道交互
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    
    public SwordStandBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SWORD_STAND.get(), pos, blockState);
    }

    /**
     * 获取剑座中的剑
     */
    public ItemStack getSword() {
        return items.getFirst();
    }

    /**
     * 设置剑座中的剑
     */
    public void setSword(ItemStack sword) {
        ItemStack copy = sword.copy();
        if (!copy.isEmpty()) copy.setCount(1);
        items.set(0, copy);
        onContentsChanged();
    }

    /**
     * 检查剑座是否为空（实现 Container#isEmpty）
     */
    @Override
    public boolean isEmpty() {
        return items.getFirst().isEmpty();
    }

    /**
     * 清空剑座
     */
    public ItemStack removeItem() {
        ItemStack result = items.getFirst().copy();
        items.set(0, ItemStack.EMPTY);
        onContentsChanged();
        return result;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // 使用 ContainerHelper 来保存物品数据
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 使用 ContainerHelper 来加载物品数据
        ContainerHelper.loadAllItems(tag, items, registries);
        
        // 确保数量为1
        ItemStack sword = items.getFirst();
        if (!sword.isEmpty() && sword.getCount() != 1) {
            sword.setCount(1);
        }
        

    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        // 先清空现有数据，防止旧数据残留
        items.set(0, ItemStack.EMPTY);
        loadAdditional(tag, lookupProvider);
    }

    @Override
    public void onDataPacket(net.minecraft.network.@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        CompoundTag tag = pkt.getTag();
        // 先清空现有数据，防止旧数据残留
        items.set(0, ItemStack.EMPTY);
        loadAdditional(tag, lookupProvider);
    }

    // ---------------- Container / WorldlyContainer 实现 ----------------

    private void onContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            // 强制同步方块实体数据到客户端 - 使用所有更新标志
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3 | 8 | 16);
        } else if (level != null) {
            // 客户端强制请求重新渲染
            Objects.requireNonNull(level.getModelDataManager()).requestRefresh(this);
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    // 无需实现 isEmptyContainer，使用 Container#isEmpty

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.getFirst();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, 0, amount);
        if (!result.isEmpty()) {
            onContentsChanged();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = items.getFirst();
        items.set(0, ItemStack.EMPTY);
        return result;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) copy.setCount(1);
        items.set(0, copy);
        onContentsChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this;
    }

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
        onContentsChanged();
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        // 仅允许放入“剑”
        return stack.getItem() instanceof SwordItem && items.getFirst().isEmpty();
    }
}
