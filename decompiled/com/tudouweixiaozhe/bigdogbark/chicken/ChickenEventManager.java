package com.tudouweixiaozhe.bigdogbark.chicken;

import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ChickenEventManager {
   private static final Map<UUID, Integer> FAILURE_SOUNDS = new HashMap<>();
   private static final Map<UUID, ChickenEventManager.EasterSession> EASTER = new HashMap<>();
   private static final Map<UUID, ChickenEventManager.PendingEgg> PENDING_EGGS = new HashMap<>();
   private static final Map<UUID, ChickenEventManager.ActiveJump> ACTIVE_JUMPS = new HashMap<>();

   private ChickenEventManager() {
   }

   public static void onEggThrown(ServerPlayer player) {
      EASTER.remove(player.getUUID());
      PENDING_EGGS.remove(player.getUUID());
   }

   public static void onEggHit(ServerLevel level, ServerPlayer player, Vec3 position, int chicks) {
      UUID playerId = player.getUUID();
      SoundEvent sound;
      if (chicks == 0) {
         int index = FAILURE_SOUNDS.getOrDefault(playerId, 0);

         sound = switch (index) {
            case 0 -> ModSounds.DING;
            case 1 -> ModSounds.DONG;
            default -> ModSounds.CHICK;
         };
         FAILURE_SOUNDS.put(playerId, (index + 1) % 3);
      } else {
         FAILURE_SOUNDS.put(playerId, 0);

         sound = switch (chicks) {
            case 1 -> ModSounds.DING_DONG_CHICK;
            case 2 -> ModSounds.DOUBLE_CHICK;
            case 3 -> ModSounds.TRIPLE_CHICK;
            default -> ModSounds.QUADRUPLE_CHICK;
         };
      }

      ModSounds.play(level, position, sound);
      if (chicks == 2) {
         long now = level.getGameTime();
         EASTER.put(playerId, new ChickenEventManager.EasterSession(now + 200L, now, new HashSet<>()));
      }
   }

   public static void onWolfTameFailed(ServerPlayer player) {
      EASTER.remove(player.getUUID());
      PENDING_EGGS.remove(player.getUUID());
   }

   public static void onWolfTamed(ServerPlayer player, UUID wolfId, long now) {
      ChickenEventManager.EasterSession session = EASTER.get(player.getUUID());
      if (session != null && now <= session.deadline() && session.wolves().add(wolfId)) {
         session.nextAudioTick = now + ModSounds.durationTicks(ModSounds.BIGDOG);
         if (session.wolves().size() >= 2) {
            PENDING_EGGS.put(player.getUUID(), new ChickenEventManager.PendingEgg(session.nextAudioTick));
            EASTER.remove(player.getUUID());
         }
      }
   }

   public static void tick(MinecraftServer server) {
      Iterator<Entry<UUID, ChickenEventManager.EasterSession>> iterator = EASTER.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, ChickenEventManager.EasterSession> entry = iterator.next();
         ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
         if (player == null || player.level().getGameTime() > entry.getValue().deadline()) {
            iterator.remove();
         }
      }

      iterator = PENDING_EGGS.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, ChickenEventManager.PendingEgg> entry = iterator.next();
         ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
         if (player == null) {
            iterator.remove();
         } else {
            ServerLevel level = player.level();
            if (level.getGameTime() >= entry.getValue().startTick()) {
               Vec3 center = player.position();
               ModSounds.play(level, center, ModSounds.EGG);
               ACTIVE_JUMPS.put(entry.getKey(), new ChickenEventManager.ActiveJump(level, center, level.getGameTime() + ModSounds.durationTicks(ModSounds.EGG)));
               iterator.remove();
            }
         }
      }

      iterator = ACTIVE_JUMPS.entrySet().iterator();

      while (iterator.hasNext()) {
         ChickenEventManager.ActiveJump jump = (ChickenEventManager.ActiveJump)iterator.next().getValue();
         if (jump.level().getGameTime() >= jump.endTick()) {
            iterator.remove();
         } else {
            AABB area = new AABB(jump.center(), jump.center()).inflate(32.0);
            jump.level().getEntitiesOfClass(Chicken.class, area).forEach(ChickenEventManager::jump);
            jump.level().getEntitiesOfClass(Wolf.class, area).forEach(ChickenEventManager::jump);
         }
      }
   }

   private static void jump(Mob mob) {
      if (mob.isAlive() && mob.onGround()) {
         mob.getJumpControl().jump();
      }
   }

   public static void clear(UUID playerId) {
      FAILURE_SOUNDS.remove(playerId);
      EASTER.remove(playerId);
      PENDING_EGGS.remove(playerId);
      ACTIVE_JUMPS.remove(playerId);
   }

   public static void clearAll() {
      FAILURE_SOUNDS.clear();
      EASTER.clear();
      PENDING_EGGS.clear();
      ACTIVE_JUMPS.clear();
   }

   private record ActiveJump(ServerLevel level, Vec3 center, long endTick) {
   }

   private static final class EasterSession {
      private final long deadline;
      private long nextAudioTick;
      private final Set<UUID> wolves;

      private EasterSession(long deadline, long nextAudioTick, Set<UUID> wolves) {
         this.deadline = deadline;
         this.nextAudioTick = nextAudioTick;
         this.wolves = wolves;
      }

      long deadline() {
         return this.deadline;
      }

      Set<UUID> wolves() {
         return this.wolves;
      }
   }

   private record PendingEgg(long startTick) {
   }
}
