package org.chxjj.bigdogbark.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

record MiningState(
   ResourceKey<Level> dimension,
   BlockPos pos,
   BlockState originalBlock,
   int selectedSlot,
   ItemStack originalTool,
   int estimatedTicks,
   int soundCount,
   boolean cursed,
   boolean instant,
   long startedAt,
   float progress,
   long nextSoundAt
) {
   MiningState withProgress(float newProgress, long newNextSoundAt) {
      return new MiningState(
         this.dimension,
         this.pos,
         this.originalBlock,
         this.selectedSlot,
         this.originalTool,
         this.estimatedTicks,
         this.soundCount,
         this.cursed,
         this.instant,
         this.startedAt,
         newProgress,
         newNextSoundAt
      );
   }
}
