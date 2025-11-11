package com.leclowndu93150.mythicdimreturn.event;

import com.leclowndu93150.mythicdimreturn.Mythicdimreturn;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlayerTickHandler {
    private static int tickCounter = 0;
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PlayerTickHandler::onServerTick);
    }
    
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        if (tickCounter >= 20) {
            tickCounter = 0;
            
            if (Mythicdimreturn.restrictionManager != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    Mythicdimreturn.restrictionManager.tickPlayer(player);
                }
            }
        }
    }
}
