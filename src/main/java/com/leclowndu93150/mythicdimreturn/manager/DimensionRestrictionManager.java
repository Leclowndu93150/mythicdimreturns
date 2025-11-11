package com.leclowndu93150.mythicdimreturn.manager;

import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionRestrictionManager {
    private final DimensionRestrictionConfig config;
    private LuckPerms luckPerms;
    
    private final Map<UUID, PlayerRestrictionData> playerData = new HashMap<>();
    
    private static class PlayerRestrictionData {
        int countdown;
        int warnings;
        boolean countdownActive;
        long lastPermissionCheck;
    }
    
    public DimensionRestrictionManager(DimensionRestrictionConfig config) {
        this.config = config;
    }
    
    private LuckPerms getLuckPerms() {
        if (luckPerms == null) {
            try {
                luckPerms = LuckPermsProvider.get();
            } catch (IllegalStateException e) {
            }
        }
        return luckPerms;
    }
    
    public boolean canEnterDimension(ServerPlayer player, ResourceKey<Level> dimension) {
        LuckPerms api = getLuckPerms();
        if (api == null) return true;
        
        DimensionRestrictionConfig.DimensionRestriction restriction = config.getRestriction(dimension);
        if (restriction == null) return true;
        
        User user = api.getUserManager().getUser(player.getUUID());
        if (user == null) return false;
        
        return user.getCachedData().getPermissionData().checkPermission(restriction.requiredPermission).asBoolean();
    }
    
    public void tickPlayer(ServerPlayer player) {
        LuckPerms api = getLuckPerms();
        if (api == null) return;
        
        ResourceKey<Level> currentDimension = player.level().dimension();
        DimensionRestrictionConfig.DimensionRestriction restriction = config.getRestriction(currentDimension);
        
        if (restriction == null) {
            playerData.remove(player.getUUID());
            return;
        }
        
        User user = api.getUserManager().getUser(player.getUUID());
        if (user == null) return;
        
        boolean hasPermission = user.getCachedData().getPermissionData().checkPermission(restriction.requiredPermission).asBoolean();
        
        PlayerRestrictionData data = playerData.computeIfAbsent(player.getUUID(), k -> new PlayerRestrictionData());
        
        if (hasPermission) {
            if (data.countdownActive) {
                long timeSinceLastCheck = System.currentTimeMillis() - data.lastPermissionCheck;
                if (timeSinceLastCheck < 5000) {
                    data.warnings++;
                    if (data.warnings >= restriction.maxWarnings) {
                        teleportPlayer(player, restriction);
                        player.sendSystemMessage(Component.literal("§cYou have been kicked for permission abuse!"));
                        playerData.remove(player.getUUID());
                        return;
                    }
                    player.sendSystemMessage(Component.literal("§eWarning: Don't abuse permissions! (" + data.warnings + "/" + restriction.maxWarnings + ")"));
                }
                data.countdownActive = false;
            }
            data.lastPermissionCheck = System.currentTimeMillis();
            return;
        }
        
        if (!data.countdownActive) {
            data.countdownActive = true;
            data.countdown = restriction.countdownSeconds;
            data.lastPermissionCheck = System.currentTimeMillis();
        }
        
        if (data.countdown > 0) {
            String message = restriction.warningMessage
                    .replace("{countdown}", String.valueOf(data.countdown))
                    .replace("&", "§");
            player.sendSystemMessage(Component.literal(message));
            data.countdown--;
        } else {
            teleportPlayer(player, restriction);
            playerData.remove(player.getUUID());
        }
    }
    
    private void teleportPlayer(ServerPlayer player, DimensionRestrictionConfig.DimensionRestriction restriction) {
        ServerLevel targetLevel = player.server.getLevel(restriction.getTeleportDimensionKey());
        if (targetLevel == null) {
            System.err.println("Target dimension not found: " + restriction.teleportLocation.dimension);
            return;
        }
        
        Vec3 pos = new Vec3(
                restriction.teleportLocation.x,
                restriction.teleportLocation.y,
                restriction.teleportLocation.z
        );
        
        player.teleportTo(targetLevel, pos.x, pos.y, pos.z, restriction.teleportLocation.yaw, restriction.teleportLocation.pitch);
        player.sendSystemMessage(Component.literal("§cYou have been teleported back to spawn."));
    }
    
    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
    }
}
