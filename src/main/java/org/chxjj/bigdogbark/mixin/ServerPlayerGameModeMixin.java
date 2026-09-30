package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.mining.MiningStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
   @Shadow
   protected ServerLevel level;
   @Shadow
   @Final
   protected ServerPlayer player;

   @Shadow
   public abstract GameType getGameModeForPlayer();

   @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
   private void bigDogBark$handleAction(BlockPos pos, Action action, Direction direction, int maxBuildHeight, int sequence, CallbackInfo ci) {
      MiningStateManager.onAction(this.level, this.player, pos, action, this.getGameModeForPlayer());
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void bigDogBark$tickMining(CallbackInfo ci) {
      MiningStateManager.tick(this.level, this.player);
   }

   @Inject(method = "destroyBlock", at = @At("RETURN"))
   private void bigDogBark$destroyed(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      MiningStateManager.onDestroyed(this.level, this.player, pos, (Boolean)cir.getReturnValue());
   }
}
