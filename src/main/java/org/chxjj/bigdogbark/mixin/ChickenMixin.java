package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.chicken.ChickenGazeManager;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public abstract class ChickenMixin {
   @Inject(method = "aiStep", at = @At("TAIL"))
   private void bigDogBark$gazeAtPlayer(CallbackInfo ci) {
      ChickenGazeManager.tick((Chicken)(Object)this);
   }
}
