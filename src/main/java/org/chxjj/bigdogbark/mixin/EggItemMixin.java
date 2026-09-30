package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.chicken.ChickenEventManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public abstract class EggItemMixin {
   @Inject(method = "use", at = @At("HEAD"))
   private void bigDogBark$newEggCancelsEaster(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
         ChickenEventManager.onEggThrown(serverPlayer);
      }
   }
}
