package com.tudouweixiaozhe.bigdogbark.reward;

import com.tudouweixiaozhe.bigdogbark.BigDogBarkMod;
import com.tudouweixiaozhe.bigdogbark.item.ModItems;
import java.util.Iterator;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class TamingRewardManager {
   private TamingRewardManager() {
   }

   public static void recordWolfTame(ServerPlayer player) {
      AdvancementHolder counter = player.level().getServer().getAdvancements().get(BigDogBarkMod.id("tame_ten_wolves_progress"));
      if (counter != null) {
         AdvancementProgress progress = player.getAdvancements().getOrStartProgress(counter);
         if (!progress.isDone()) {
            Iterator<String> remaining = progress.getRemainingCriteria().iterator();
            if (remaining.hasNext()) {
               player.getAdvancements().award(counter, remaining.next());
               if (player.getAdvancements().getOrStartProgress(counter).isDone()) {
                  ItemStack record = ModItems.EH_BIG_DOG_DISC.getDefaultInstance();
                  if (!player.addItem(record)) {
                     player.spawnAtLocation(player.level(), record);
                  }
               }
            }
         }
      }
   }
}
