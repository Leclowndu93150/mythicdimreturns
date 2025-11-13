package com.leclowndu93150.mythicdimreturn.command;

import com.leclowndu93150.mythicdimreturn.Mythicdimreturn;
import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import com.leclowndu93150.mythicdimreturn.event.DoubleJumpHandler;
import com.leclowndu93150.mythicdimreturn.manager.DimensionRestrictionManager;
import com.leclowndu93150.mythicdimreturn.manager.DoubleJumpManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mythicdimreturn")
            .requires(source -> source.hasPermission(4))
            .then(Commands.literal("reload")
                .executes(ReloadCommand::reloadConfig)
            )
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        DimensionRestrictionConfig config = DimensionRestrictionConfig.load();
        Mythicdimreturn.restrictionManager = new DimensionRestrictionManager(config);
        Mythicdimreturn.doubleJumpManager = new DoubleJumpManager(config);
        
        DoubleJumpHandler.register(Mythicdimreturn.doubleJumpManager, config);
        DoubleJumpCommand.setManager(Mythicdimreturn.doubleJumpManager);
        
        context.getSource().sendSuccess(
            () -> Component.literal("MythicDimReturn configuration reloaded successfully!"),
            true
        );
        return 1;
    }
}
