package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.chicken.ChickenEventManager;
import org.chxjj.bigdogbark.enchantment.BigDogBarkEnchantment;
import org.chxjj.bigdogbark.reward.TamingRewardManager;
import org.chxjj.bigdogbark.sound.ModSounds;
import org.chxjj.bigdogbark.wolf.WolfVisualEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {
   @Unique
   private boolean bigDogBark$wasTame;
   @Unique
   private boolean bigDogBark$wasSittingBeforeSet;

   @Inject(method = "setInSittingPose", at = @At("HEAD"))
   private void bigDogBark$rememberSittingState(boolean sitting, CallbackInfo ci) {
      this.bigDogBark$wasSittingBeforeSet = ((TamableAnimal)(Object)this).isInSittingPose();
   }

   @Inject(method = "setInSittingPose", at = @At("TAIL"))
   private void bigDogBark$playSittingChangeSound(boolean sitting, CallbackInfo ci) {
      TamableAnimal animal = (TamableAnimal)(Object)this;
      if (animal instanceof Wolf
         && animal.tickCount > 0
         && this.bigDogBark$wasSittingBeforeSet != animal.isInSittingPose()
         && animal.level() instanceof ServerLevel level) {
         ModSounds.play(level, animal.position(), ModSounds.EH);
      }
   }

   @Inject(method = "tame", at = @At("HEAD"))
   private void bigDogBark$rememberTameState(Player player, CallbackInfo ci) {
      this.bigDogBark$wasTame = ((TamableAnimal)(Object)this).isTame();
   }

   @Inject(method = "tame", at = @At("TAIL"))
   private void bigDogBark$rewardWolfTaming(Player player, CallbackInfo ci) {
      TamableAnimal animal = (TamableAnimal)(Object)this;
      if (!this.bigDogBark$wasTame
         && animal instanceof Wolf
         && player instanceof ServerPlayer serverPlayer
         && animal.level() instanceof ServerLevel level
         && animal.isTame()) {
         ItemStack reward = BigDogBarkEnchantment.createBook(level.registryAccess());
         if (!serverPlayer.addItem(reward)) {
            serverPlayer.spawnAtLocation(level, reward);
         }

         ModSounds.play(level, animal.position(), ModSounds.BIGDOG);
         WolfVisualEvents.send((Wolf)animal, (byte)2, serverPlayer);
         TamingRewardManager.recordWolfTame(serverPlayer);
         ChickenEventManager.onWolfTamed(serverPlayer, animal.getUUID(), level.getGameTime());
      }
   }
}
