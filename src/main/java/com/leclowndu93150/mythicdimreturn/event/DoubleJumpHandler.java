package com.leclowndu93150.mythicdimreturn.event;

import com.leclowndu93150.mythicdimreturn.config.DimensionRestrictionConfig;
import com.leclowndu93150.mythicdimreturn.manager.DoubleJumpManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DoubleJumpHandler {
    private static DoubleJumpManager doubleJumpManager;
    private static DimensionRestrictionConfig config;
    
    public static void register(DoubleJumpManager manager, DimensionRestrictionConfig cfg) {
        doubleJumpManager = manager;
        config = cfg;
        
        ServerTickEvents.END_SERVER_TICK.register(DoubleJumpHandler::onServerTick);
    }
    
    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            doubleJumpManager.tick(player);
            UUID playerId = player.getUUID();
            
            if (player.getAbilities().instabuild) continue;
            
            DimensionRestrictionConfig.DoubleJumpConfig djConfig = 
                config.getDoubleJumpConfig(player.level().dimension());
            
            if (djConfig == null) continue;
            
            DoubleJumpManager.PlayerDoubleJumpData data = doubleJumpManager.getPlayerData(playerId);
            
            boolean isSneaking = player.isCrouching();
            boolean isOnGround = player.onGround();
            
            if (isOnGround) {
                data.wasSneaking = false;
            } else {
                if (isSneaking && !data.wasSneaking) {
                    if (doubleJumpManager.canDoubleJump(player)) {
                        performDoubleJump(player);
                    }
                }
                data.wasSneaking = isSneaking;
            }
        }
    }
    
    private static void performDoubleJump(ServerPlayer player) {
        DimensionRestrictionConfig.DoubleJumpConfig djConfig = 
            config.getDoubleJumpConfig(player.level().dimension());
        
        if (djConfig == null) return;
        
        Vec3 lookAngle = player.getLookAngle();
        Vec3 velocity = new Vec3(
            lookAngle.x * 0.5, 
            0.6, 
            lookAngle.z * 0.5
        );
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        
        ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(player.getId(), velocity);
        player.connection.send(packet);
        
        spawnDoubleJumpParticles(player);
        
        doubleJumpManager.performDoubleJump(player);
        
        int remaining = doubleJumpManager.getRemainingJumps(player.getUUID(), djConfig);
        String message = djConfig.jumpMessage
            .replace("{remaining}", String.valueOf(remaining))
            .replace("{max}", String.valueOf(djConfig.maxDoubleJumps))
            .replace("&0", "§0").replace("&1", "§1").replace("&2", "§2").replace("&3", "§3")
            .replace("&4", "§4").replace("&5", "§5").replace("&6", "§6").replace("&7", "§7")
            .replace("&8", "§8").replace("&9", "§9").replace("&a", "§a").replace("&b", "§b")
            .replace("&c", "§c").replace("&d", "§d").replace("&e", "§e").replace("&f", "§f")
            .replace("&k", "§k").replace("&l", "§l").replace("&m", "§m").replace("&n", "§n")
            .replace("&o", "§o").replace("&r", "§r");
        
        player.displayClientMessage(Component.literal(message), true);
    }
    
    private static void spawnDoubleJumpParticles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position();
        
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            
            level.sendParticles(
                ParticleTypes.CLOUD,
                pos.x + offsetX,
                pos.y + offsetY,
                pos.z + offsetZ,
                1,
                0, -0.1, 0,
                0.05
            );
        }
    }
    
    public static void cleanup(UUID playerId) {
        doubleJumpManager.removePlayerData(playerId);
    }
}
