package com.leclowndu93150.mythicdimreturn.command;

import com.leclowndu93150.mythicdimreturn.manager.DoubleJumpManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class DoubleJumpCommand {
    private static DoubleJumpManager doubleJumpManager;
    
    public static void setManager(DoubleJumpManager manager) {
        doubleJumpManager = manager;
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("doublejump")
            .requires(source -> source.hasPermission(4))
            .then(Commands.literal("refill")
                .then(Commands.argument("player", EntityArgument.players())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> refillDoubleJumps(
                            context,
                            EntityArgument.getPlayers(context, "player"),
                            IntegerArgumentType.getInteger(context, "amount")
                        ))
                    )
                )
            )
        );
    }

    private static int refillDoubleJumps(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, int amount) {
        if (doubleJumpManager == null) {
            context.getSource().sendFailure(Component.literal("DoubleJumpManager not initialized!"));
            return 0;
        }
        
        for (ServerPlayer player : players) {
            doubleJumpManager.refillJumps(player.getUUID(), amount);
            context.getSource().sendSuccess(
                () -> Component.literal("Refilled " + amount + " double jump(s) for " + player.getName().getString()),
                true
            );
        }
        return players.size();
    }
}
