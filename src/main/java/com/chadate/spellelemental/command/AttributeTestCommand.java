package com.chadate.spellelemental.command;

import com.chadate.spellelemental.util.AttributeModifierUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * 属性效果测试命令
 * 用于测试物理抗性下降效果和粒子播放
 */
public class AttributeTestCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("attribute_test")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .then(Commands.literal("physical_resistance")
                    .then(Commands.literal("apply")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .then(Commands.argument("percentage", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    LivingEntity target = (LivingEntity) EntityArgument.getEntity(context, "target");
                                    double percentage = DoubleArgumentType.getDouble(context, "percentage");
                                    
                                    AttributeModifierUtil.applyTemporaryPhysicalResistanceDown(target, percentage);
                                    
                                    context.getSource().sendSuccess(() -> 
                                        Component.literal(String.format("已为 %s 应用 %.1f%% 的物理抗性下降效果", 
                                            target.getName().getString(), percentage * 100)), 
                                        true);
                                    
                                    return 1;
                                })
                            )
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(context -> {
                                LivingEntity target = (LivingEntity) EntityArgument.getEntity(context, "target");
                                
                                AttributeModifierUtil.removePhysicalResistanceDown(target);
                                
                                context.getSource().sendSuccess(() -> 
                                    Component.literal(String.format("已移除 %s 的物理抗性下降效果", 
                                        target.getName().getString())), 
                                    true);
                                
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("check")
                        .then(Commands.argument("target", EntityArgument.entity())
                            .executes(context -> {
                                LivingEntity target = (LivingEntity) EntityArgument.getEntity(context, "target");
                                
                                boolean hasEffect = AttributeModifierUtil.hasPhysicalResistanceDown(target);
                                double amount = AttributeModifierUtil.getPhysicalResistanceDownAmount(target);
                                
                                if (hasEffect) {
                                    context.getSource().sendSuccess(() -> 
                                        Component.literal(String.format("%s 当前有物理抗性下降效果：%.1f%%", 
                                            target.getName().getString(), amount * 100)), 
                                        false);
                                } else {
                                    context.getSource().sendSuccess(() -> 
                                        Component.literal(String.format("%s 当前没有物理抗性下降效果", 
                                            target.getName().getString())), 
                                        false);
                                }
                                
                                return 1;
                            })
                        )
                    )
                )
        );
    }
}
