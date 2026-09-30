package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.combat.DamageTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
   @Inject(method = "hurtServer", at = @At("HEAD"))
   private void bigDogBark$beforeDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      DamageTracker.before((LivingEntity)(Object)this);
   }

   @Inject(method = "hurtServer", at = @At("RETURN"))
   private void bigDogBark$afterDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      DamageTracker.after((LivingEntity)(Object)this, level, source, (Boolean)cir.getReturnValue());
   }
}
