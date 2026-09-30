package com.tudouweixiaozhe.bigdogbark.mixin;

import com.tudouweixiaozhe.bigdogbark.item.ModItems;
import com.tudouweixiaozhe.bigdogbark.jukebox.JukeboxDogDanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private static void bigDogBark$animateWolves(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity jukebox, CallbackInfo ci) {
      if (level instanceof ServerLevel serverLevel && jukebox.getSongPlayer().isPlaying() && jukebox.getTheItem().is(ModItems.EH_BIG_DOG_DISC)) {
         JukeboxDogDanceManager.animate(serverLevel, pos);
      }
   }
}
