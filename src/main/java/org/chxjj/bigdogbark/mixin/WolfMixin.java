package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.chicken.ChickenEventManager;
import org.chxjj.bigdogbark.debug.ProbabilitySettings;
import org.chxjj.bigdogbark.enchantment.NoBarkCurseEnchantment;
import org.chxjj.bigdogbark.sound.ModSounds;
import org.chxjj.bigdogbark.wolf.WolfVisualEvents;
import org.chxjj.bigdogbark.wolf.WolfVisualStateAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin implements WolfVisualStateAccess {
   @Unique
   private static final EntityDataAccessor<Byte> BIG_DOG_BARK_MUSIC_MODE = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BYTE);
   @Unique
   private ServerPlayer bigDogBark$tamingPlayer;
   @Unique
   private boolean bigDogBark$deathRewarded;

   @Inject(method = "defineSynchedData", at = @At("TAIL"))
   private void bigDogBark$defineVisualState(Builder builder, CallbackInfo ci) {
      builder.define(BIG_DOG_BARK_MUSIC_MODE, (byte)0);
   }

   @Override
   public byte bigDogBark$getMusicMode() {
      return (Byte)((Wolf)(Object)this).getEntityData().get(BIG_DOG_BARK_MUSIC_MODE);
   }

   @Override
   public void bigDogBark$setMusicMode(byte mode) {
      ((Wolf)(Object)this).getEntityData().set(BIG_DOG_BARK_MUSIC_MODE, mode);
   }

   @Redirect(method = "tryToTame", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
   private int bigDogBark$debugTameChance(RandomSource random, int vanillaBound) {
      return ProbabilitySettings.rollTame(random, vanillaBound) ? 0 : 1;
   }

   @Inject(method = "mobInteract", at = @At("HEAD"))
   private void bigDogBark$rememberBoneAttempt(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      Wolf wolf = (Wolf)(Object)this;
      this.bigDogBark$tamingPlayer = !wolf.isTame() && player instanceof ServerPlayer serverPlayer && player.getItemInHand(hand).is(Items.BONE)
         ? serverPlayer
         : null;
   }

   @Inject(method = "mobInteract", at = @At("RETURN"))
   private void bigDogBark$playFailedTame(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      ServerPlayer attemptedBy = this.bigDogBark$tamingPlayer;
      this.bigDogBark$tamingPlayer = null;
      Wolf wolf = (Wolf)(Object)this;
      if (attemptedBy != null && !wolf.isTame() && wolf.level() instanceof ServerLevel level) {
         ModSounds.play(level, wolf.position(), ModSounds.EH);
         WolfVisualEvents.send(wolf, (byte)1, attemptedBy);
         ChickenEventManager.onWolfTameFailed(attemptedBy);
      }
   }

   @Inject(method = "die", at = @At("HEAD"))
   private void bigDogBark$giveOwnerCurse(DamageSource source, CallbackInfo ci) {
      Wolf wolf = (Wolf)(Object)this;
      if (!this.bigDogBark$deathRewarded && wolf.isTame() && wolf.level() instanceof ServerLevel level) {
         this.bigDogBark$deathRewarded = true;
         EntityReference<LivingEntity> ownerReference = wolf.getOwnerReference();
         if (ownerReference != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerReference.getUUID());
            if (owner != null) {
               ItemStack reward = NoBarkCurseEnchantment.createBook(level.registryAccess());
               if (!owner.addItem(reward)) {
                  owner.spawnAtLocation(owner.level(), reward);
               }
            }
         }
      }
   }
}
