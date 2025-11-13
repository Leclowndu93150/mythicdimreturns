package com.leclowndu93150.mythicdimreturn.manager;

import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoubleJumpManager {
    public final DimensionRestrictionConfig config;
    private final Map<UUID, PlayerDoubleJumpData> playerData = new HashMap<>();
    
    public DoubleJumpManager(DimensionRestrictionConfig config) {
        this.config = config;
    }
    
    public PlayerDoubleJumpData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerDoubleJumpData());
    }
    
    public void removePlayerData(UUID playerId) {
        playerData.remove(playerId);
    }
    
    public void refillJumps(UUID playerId, int amount) {
        PlayerDoubleJumpData data = getPlayerData(playerId);
        data.manualJumps += amount;
    }
    
    public void tick(ServerPlayer player) {
        PlayerDoubleJumpData data = getPlayerData(player.getUUID());
        DimensionRestrictionConfig.DoubleJumpConfig djConfig = config.getDoubleJumpConfig(player.level().dimension());
        
        if (djConfig == null) return;
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRefill = currentTime - data.lastRefillTime;
        long refillInterval = djConfig.refill.secondsPerRefill * 1000L;
        
        if (timeSinceLastRefill >= refillInterval) {
            if (data.autoJumps < djConfig.refill.maxAutoRefill) {
                data.autoJumps++;
                data.lastRefillTime = currentTime;
            }
        }
    }
    
    public boolean canDoubleJump(ServerPlayer player) {
        DimensionRestrictionConfig.DoubleJumpConfig djConfig = config.getDoubleJumpConfig(player.level().dimension());
        if (djConfig == null) return false;
        
        if (!hasAccess(player, djConfig)) return false;
        
        PlayerDoubleJumpData data = getPlayerData(player.getUUID());
        long currentTime = System.currentTimeMillis();
        long cooldown = djConfig.cooldownSeconds * 1000L;
        
        if (currentTime - data.lastJumpTime < cooldown) {
            return false;
        }
        
        return getRemainingJumps(player.getUUID(), djConfig) > 0;
    }
    
    public void performDoubleJump(ServerPlayer player) {
        PlayerDoubleJumpData data = getPlayerData(player.getUUID());
        
        if (data.autoJumps > 0) {
            data.autoJumps--;
        } else if (data.manualJumps > 0) {
            data.manualJumps--;
        }
        
        data.lastJumpTime = System.currentTimeMillis();
    }
    
    public int getRemainingJumps(UUID playerId, DimensionRestrictionConfig.DoubleJumpConfig djConfig) {
        PlayerDoubleJumpData data = getPlayerData(playerId);
        return Math.min(data.autoJumps + data.manualJumps, djConfig.maxDoubleJumps);
    }
    
    private boolean hasAccess(ServerPlayer player, DimensionRestrictionConfig.DoubleJumpConfig djConfig) {
        if (djConfig.accessRules.isEmpty()) return true;
        
        for (DimensionRestrictionConfig.DoubleJumpConfig.AccessRule rule : djConfig.accessRules) {
            if (rule.permission != null && !rule.permission.isEmpty()) {
                if (hasPermission(player, rule.permission)) {
                    return true;
                }
            }
            
            if (rule.armorRequirement != null) {
                if (matchesArmorRequirement(player, rule.armorRequirement)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean hasPermission(ServerPlayer player, String permission) {
        return player.hasPermissions(4) || player.getServer().getPlayerList().isOp(player.getGameProfile());
    }
    
    private boolean matchesArmorRequirement(ServerPlayer player, DimensionRestrictionConfig.DoubleJumpConfig.AccessRule.ArmorRequirement armor) {
        if (!armor.helmet.isEmpty()) {
            String helmet = player.getInventory().getArmor(3).getDescriptionId();
            if (!helmet.contains(armor.helmet.replace("minecraft:", ""))) return false;
        }
        
        if (!armor.chestplate.isEmpty()) {
            String chestplate = player.getInventory().getArmor(2).getDescriptionId();
            if (!chestplate.contains(armor.chestplate.replace("minecraft:", ""))) return false;
        }
        
        if (!armor.leggings.isEmpty()) {
            String leggings = player.getInventory().getArmor(1).getDescriptionId();
            if (!leggings.contains(armor.leggings.replace("minecraft:", ""))) return false;
        }
        
        if (!armor.boots.isEmpty()) {
            String boots = player.getInventory().getArmor(0).getDescriptionId();
            if (!boots.contains(armor.boots.replace("minecraft:", ""))) return false;
        }
        
        return true;
    }
    
    public static class PlayerDoubleJumpData {
        public int autoJumps = 0;
        public int manualJumps = 0;
        public long lastJumpTime = 0;
        public long lastRefillTime = System.currentTimeMillis();
        public boolean wasSneaking = false;
    }
}
