package com.tudouweixiaozhe.bigdogbark.anvil;

import net.minecraft.server.level.ServerPlayer;

public final class AnvilSoundController {
   private static final ThreadLocal<ServerPlayer> ACTIVE = new ThreadLocal<>();

   private AnvilSoundController() {
   }

   public static void begin(ServerPlayer player) {
      ACTIVE.set(player);
   }

   public static ServerPlayer activePlayer() {
      return ACTIVE.get();
   }

   public static void end() {
      ACTIVE.remove();
   }
}
