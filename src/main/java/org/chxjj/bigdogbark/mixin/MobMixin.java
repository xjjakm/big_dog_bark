package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.sound.ModSounds;
import org.chxjj.bigdogbark.wolf.WolfVisualEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {
   @Inject(method = "doHurtTarget", at = @At("RETURN"))
   private void bigDogBark$playTamedWolfHit(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValueZ() && (Object)this instanceof Wolf wolf && wolf.isTame()) {
         boolean killed = target instanceof LivingEntity living && (!living.isAlive() || living.getHealth() <= 0.0F);
         if (killed) {
            ModSounds.play(level, target.position(), ModSounds.SHOUT);
         } else {
            ModSounds.play(level, wolf.position(), wolf.isBaby() ? ModSounds.SMALL_SHOUT : ModSounds.DOG_ANVIL);
            WolfVisualEvents.send(wolf, (byte)3, null);
         }
      }
   }
}
