package com.chadate.spellelemental.command;

import com.chadate.spellelemental.client.network.custom.ElementData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DebugCommand {
	private DebugCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal("spell_debug")
				.requires(src -> src.hasPermission(0))
				.then(Commands.literal("toggle")
					.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(ctx -> toggle(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled"))))
				)
		);
	}

	private static int toggle(CommandSourceStack source, boolean enabled) {
		if (source.getEntity() instanceof ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, new ElementData.ElementDebugToggle(enabled));
		}
		return Command.SINGLE_SUCCESS;
	}
} 