package com.chadate.spellelemental.block;

import com.chadate.spellelemental.block.entity.SwordStandBlockEntity;
import com.chadate.spellelemental.config.ServerConfig;
import com.chadate.spellelemental.integration.jei.data.SwordOilConfigLoader;
import com.chadate.spellelemental.util.ActionBarMessageUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 剑座方块类
 * 允许玩家放置和展示剑类武器
 */
public class SwordStandBlock extends BaseEntityBlock {
    
    public static final MapCodec<SwordStandBlock> CODEC = simpleCodec(SwordStandBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    // 剑座的碰撞箱 - 根据模型文件设计 (朝北方向的基础形状)
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
        Block.box(1, 0, 4, 15, 1, 12),   // 底座：14x1x8 (from [1,0,4] to [15,1,12])
        Block.box(2, 1, 5, 14, 2, 11)    // 上层平台：12x1x6 (from [2,1,5] to [14,2,11])
    );
    
    // 不同方向的碰撞箱
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
        Block.box(1, 0, 4, 15, 1, 12),   // 底座
        Block.box(2, 1, 5, 14, 2, 11)    // 上层平台
    );
    
    private static final VoxelShape EAST_SHAPE = Shapes.or(
        Block.box(4, 0, 1, 12, 1, 15),   // 底座旋转90度
        Block.box(5, 1, 2, 11, 2, 14)    // 上层平台旋转90度
    );
    
    private static final VoxelShape WEST_SHAPE = Shapes.or(
        Block.box(4, 0, 1, 12, 1, 15),   // 底座旋转90度
        Block.box(5, 1, 2, 11, 2, 14)    // 上层平台旋转90度
    );

    public SwordStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SwordStandBlockEntity(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SwordStandBlockEntity swordStand)) {
            return ItemInteractionResult.FAIL;
        }


        // 如果手持剑油且剑座有剑，则为剑附着元素（软编码支持任意物品作为剑油）
        SwordOilConfigLoader.SwordOilConfig swordOilConfig = SwordOilConfigLoader.getSwordOilConfig(stack.getItem());
        if (swordOilConfig != null && !swordStand.isEmpty()) {
            ItemStack sword = swordStand.getSword();
            if (applySwordOil(sword, swordOilConfig, player)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                swordStand.setSword(sword); // 更新剑座中的剑
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.FAIL;
        }

        // 如果手持剑类物品且剑座为空，则放置剑
        if (isSwordItem(stack) && swordStand.isEmpty()) {
            ItemStack swordToPlace = stack.copy();
            swordToPlace.setCount(1);
            swordStand.setSword(swordToPlace);
            
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, 
                                             @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SwordStandBlockEntity swordStand)) {
            return InteractionResult.FAIL;
        }

        // 如果剑座有剑且玩家空手，取出剑
        if (!swordStand.isEmpty()) {
            ItemStack removedSword = swordStand.removeItem();
            if (player.addItem(removedSword)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            } else {
                // 如果背包满了，重新放回剑
                swordStand.setSword(removedSword);
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 判断物品是否为剑类物品
     */
    private boolean isSwordItem(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    /**
     * 为剑应用剑油，附着对应元素（软编码版本，支持任意物品作为剑油）
     */
    private boolean applySwordOil(ItemStack sword, SwordOilConfigLoader.SwordOilConfig swordOilConfig, Player player) {
        if (sword.isEmpty() || !(sword.getItem() instanceof SwordItem)) {
            return false;
        }

        // 从配置获取剑油的元素类型和元素量
        String elementType = swordOilConfig.getElement();
        int elementAmount = swordOilConfig.getAmount();
        
        // 从服务端配置获取武器最大元素量
        final int MAX_ELEMENT_AMOUNT = ServerConfig.getWeaponMaxElementAmount();

        // 检查剑是否已经有相同的元素附着
        CompoundTag tag = sword.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag elementData = tag.getCompound("ElementAttachment");
        
        if (elementData.contains("element")) {
            String currentElement = elementData.getString("element");
            if (currentElement.equals(elementType)) {
                // 如果已有相同元素，增加元素量（但不超过上限）
                int currentAmount = elementData.getInt("amount");
                int newAmount = Math.min(currentAmount + elementAmount, MAX_ELEMENT_AMOUNT);
                
                if (newAmount == currentAmount) {
                    // 已达到上限，无法继续添加
                    if (player instanceof ServerPlayer serverPlayer) {
                        ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer,
                            "message.spellelemental.sword_oil.limit_reached", 
                            Component.translatable("element.spellelemental." + elementType), MAX_ELEMENT_AMOUNT);
                    }
                    return false;
                } else if (newAmount < currentAmount + elementAmount) {
                    // 部分添加到上限
                    elementData.putInt("amount", newAmount);
                    if (player instanceof ServerPlayer serverPlayer) {
                        ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                            "message.spellelemental.sword_oil.partial_extended", 
                            Component.translatable("element.spellelemental." + elementType), newAmount, MAX_ELEMENT_AMOUNT);
                    }
                } else {
                    // 正常添加
                    elementData.putInt("amount", newAmount);
                    if (player instanceof ServerPlayer serverPlayer) {
                        ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                            "message.spellelemental.sword_oil.extended", 
                            Component.translatable("element.spellelemental." + elementType));
                    }
                }
            } else {
                // 如果已有不同元素，拒绝覆盖
                if (player instanceof ServerPlayer serverPlayer) {
                    ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                        "message.spellelemental.sword_oil.different_element", 
                        Component.translatable("element.spellelemental." + currentElement),
                        Component.translatable("element.spellelemental." + elementType));
                }
                return false;
            }
        } else {
            // 应用新的元素附着（限制在上限内）
            int finalAmount = Math.min(elementAmount, MAX_ELEMENT_AMOUNT);
            elementData.putString("element", elementType);
            elementData.putInt("amount", finalAmount);
            elementData.putLong("appliedTime", System.currentTimeMillis());
            tag.put("ElementAttachment", elementData);
            
            if (finalAmount < elementAmount) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                        "message.spellelemental.sword_oil.applied_limited", 
                        Component.translatable("element.spellelemental." + elementType), finalAmount, MAX_ELEMENT_AMOUNT);
                }
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    ActionBarMessageUtil.sendTranslatableActionBarMessage(serverPlayer, 
                        "message.spellelemental.sword_oil.applied", 
                        Component.translatable("element.spellelemental." + elementType));
                }
            }
        }
        
        // 保存更新后的数据到剑
        sword.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.of(tag));

        return true;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SwordStandBlockEntity swordStand) {
                if (!swordStand.isEmpty()) {
                    ItemStack drop = swordStand.removeItem();
                    if (!drop.isEmpty()) {
                        popResource(level, pos, drop);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // 方块放置时设置方向
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // 创建方块状态定义
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 支持旋转
    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // 支持镜像
    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
