package com.leclowndu93150.mythicdimreturn;

import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import com.leclowndu93150.mythicdimreturn.event.PlayerTickHandler;
import com.leclowndu93150.mythicdimreturn.manager.DimensionRestrictionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Mythicdimreturn implements ModInitializer {
    public static DimensionRestrictionManager restrictionManager;

    @Override
    public void onInitialize() {
        DimensionRestrictionConfig config = DimensionRestrictionConfig.load();
        restrictionManager = new DimensionRestrictionManager(config);
        
        PlayerTickHandler.register();
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            restrictionManager.removePlayerData(handler.getPlayer().getUUID());
        });
        
        System.out.println("MythicDimReturn initialized with " + config.restrictions.size() + " dimension restriction(s)");
    }
}
