package com.tudouweixiaozhe.bigdogbark.debug;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class DebugManager {
   private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

   private DebugManager() {
   }

   public static void set(ServerPlayer player, boolean enabled) {
      if (enabled) {
         ENABLED.add(player.getUUID());
      } else {
         ENABLED.remove(player.getUUID());
      }

      player.sendSystemMessage(Component.literal("[大狗叫] 调试模式已" + (enabled ? "开启" : "关闭")));
   }

   public static boolean isEnabled(ServerPlayer player) {
      return ENABLED.contains(player.getUUID());
   }

   public static void send(ServerPlayer player, String message) {
      if (isEnabled(player)) {
         player.sendSystemMessage(Component.literal("[大狗叫] " + message));
      }
   }

   public static void clear(UUID playerId) {
      ENABLED.remove(playerId);
   }

   public static void clearAll() {
      ENABLED.clear();
   }
}
