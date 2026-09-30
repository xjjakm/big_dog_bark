package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.chicken.ChickenEventManager;
import org.chxjj.bigdogbark.debug.ProbabilitySettings;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEgg.class)
public abstract class ThrownEggMixin {
   @Inject(
      method = "onHit",
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/world/entity/projectile/throwableitemprojectile/ThrownEgg;random:Lnet/minecraft/util/RandomSource;",
         ordinal = 0
      ),
      cancellable = true
   )
   private void bigDogBark$customHatchRates(HitResult hitResult, CallbackInfo ci) {
      ThrownEgg egg = (ThrownEgg)(Object)this;
      if (egg.level() instanceof ServerLevel level) {
         int var9 = ProbabilitySettings.rollEgg(egg.getRandom());

         for (int position = 0; position < var9; position++) {
            Chicken chicken = (Chicken)EntityTypes.CHICKEN.create(level, EntitySpawnReason.TRIGGERED);
            if (chicken != null) {
               chicken.setAge(-24000);
               chicken.snapTo(egg.getX(), egg.getY(), egg.getZ(), egg.getYRot(), 0.0F);
               Holder<ChickenVariant> variant = (Holder<ChickenVariant>)egg.getItem().get(DataComponents.CHICKEN_VARIANT);
               if (variant != null) {
                  chicken.setVariant(variant);
               }

               if (chicken.fudgePositionAfterSizeChange(EntityDimensions.fixed(0.0F, 0.0F))) {
                  level.addFreshEntity(chicken);
               }
            }
         }

         Vec3 position = egg.position();
         if (egg.getOwner() instanceof ServerPlayer player) {
            ChickenEventManager.onEggHit(level, player, position, var9);
         }

         level.broadcastEntityEvent(egg, (byte)3);
         egg.discard();
         ci.cancel();
      }
   }
}
