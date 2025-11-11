package com.leclowndu93150.mythicdimreturn.mixin;

import com.leclowndu93150.mythicdimreturn.Mythicdimreturn;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class EntityMixin {
    
    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void onChangeDimension(DimensionTransition dimensionTransition, CallbackInfoReturnable<@Nullable Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        if (dimensionTransition == null || dimensionTransition.newLevel() == null) {
            return;
        }
        
        if (player.level().dimension().equals(dimensionTransition.newLevel().dimension())) {
            return;
        }
        
        if (Mythicdimreturn.restrictionManager != null) {
            if (!Mythicdimreturn.restrictionManager.canEnterDimension(player, dimensionTransition.newLevel().dimension())) {
                player.sendSystemMessage(Component.literal("§cYou don't have permission to enter this dimension!"));
                cir.setReturnValue(null);
            }
        }
    }
}
