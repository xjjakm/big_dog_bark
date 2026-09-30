package com.tudouweixiaozhe.bigdogbark.mixin;

import com.tudouweixiaozhe.bigdogbark.anvil.AnvilSoundController;
import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class LevelMixin {
   @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
   private void bigDogBark$replaceAnvilSound(Entity source, int eventId, BlockPos pos, int data, CallbackInfo ci) {
      ServerPlayer player = AnvilSoundController.activePlayer();
      if (player != null && player.level() == (ServerLevel)this && (eventId == 1029 || eventId == 1030)) {
         ModSounds.playTo(player, Vec3.atCenterOf(pos), ModSounds.DOG_ANVIL);
         ci.cancel();
      }
   }
}
