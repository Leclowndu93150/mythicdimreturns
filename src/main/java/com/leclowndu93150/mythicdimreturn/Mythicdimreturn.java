package com.leclowndu93150.mythicdimreturn;

import com.leclowndu93150.mythicdimreturn.command.DoubleJumpCommand;
import com.leclowndu93150.mythicdimreturn.command.ReloadCommand;
import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import com.leclowndu93150.mythicdimreturn.event.DoubleJumpHandler;
import com.leclowndu93150.mythicdimreturn.event.PlayerTickHandler;
import com.leclowndu93150.mythicdimreturn.manager.DimensionRestrictionManager;
import com.leclowndu93150.mythicdimreturn.manager.DoubleJumpManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Mythicdimreturn implements ModInitializer {
    public static DimensionRestrictionManager restrictionManager;
    public static DoubleJumpManager doubleJumpManager;

    @Override
    public void onInitialize() {
        DimensionRestrictionConfig config = DimensionRestrictionConfig.load();
        restrictionManager = new DimensionRestrictionManager(config);
        doubleJumpManager = new DoubleJumpManager(config);
        
        PlayerTickHandler.register();
        DoubleJumpHandler.register(doubleJumpManager, config);
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DoubleJumpCommand.setManager(doubleJumpManager);
            DoubleJumpCommand.register(dispatcher);
            ReloadCommand.register(dispatcher);
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            restrictionManager.removePlayerData(handler.getPlayer().getUUID());
            doubleJumpManager.removePlayerData(handler.getPlayer().getUUID());
            DoubleJumpHandler.cleanup(handler.getPlayer().getUUID());
        });
        
        System.out.println("MythicDimReturn initialized with " + config.restrictions.size() + " dimension restriction(s) and " + config.doubleJump.size() + " double jump config(s)");
    }
}
