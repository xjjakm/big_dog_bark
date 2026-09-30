package org.chxjj.bigdogbark.jukebox;

import org.chxjj.bigdogbark.wolf.WolfVisualStateAccess;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class JukeboxDogDanceManager {
   private static final Map<UUID, JukeboxDogDanceManager.DanceState> DANCERS = new HashMap<>();

   private JukeboxDogDanceManager() {
   }

   public static void animate(ServerLevel level, BlockPos jukeboxPos) {
      long now = level.getGameTime();
      Vec3 center = Vec3.atCenterOf(jukeboxPos);
      AABB area = new AABB(center, center).inflate(32.0);

      for (Wolf wolf : level.getEntitiesOfClass(Wolf.class, area)) {
         if (!(wolf.distanceToSqr(center) > 1024.0)) {
            JukeboxDogDanceManager.DanceState state = DANCERS.computeIfAbsent(wolf.getUUID(), ignored -> {
               byte initial = randomMode(wolf, (byte)0);
               ((WolfVisualStateAccess)wolf).bigDogBark$setMusicMode(initial);
               return new JukeboxDogDanceManager.DanceState(wolf, now, now + 12L + wolf.getRandom().nextInt(13), initial);
            });
            state.lastSeen = now;
            if (now >= state.nextChange) {
               state.mode = randomMode(wolf, state.mode);
               ((WolfVisualStateAccess)wolf).bigDogBark$setMusicMode(state.mode);
               state.nextChange = now + 12L + wolf.getRandom().nextInt(13);
            }
         }
      }
   }

   private static byte randomMode(Wolf wolf, byte previous) {
      byte mode;
      do {
         mode = (byte)(1 + wolf.getRandom().nextInt(3));
      } while (mode == previous);

      return mode;
   }

   public static void finishServerTick(MinecraftServer server) {
      Iterator<JukeboxDogDanceManager.DanceState> iterator = DANCERS.values().iterator();

      while (iterator.hasNext()) {
         JukeboxDogDanceManager.DanceState state = iterator.next();
         Wolf wolf = state.wolf;
         if (!(wolf.isAlive() && wolf.level() instanceof ServerLevel level)) {
            iterator.remove();
         } else if (state.lastSeen < level.getGameTime()) {
            ((WolfVisualStateAccess)wolf).bigDogBark$setMusicMode((byte)0);
            iterator.remove();
         }
      }
   }

   public static void clearAll() {
      DANCERS.values().forEach(state -> {
         if (state.wolf.isAlive()) {
            ((WolfVisualStateAccess)state.wolf).bigDogBark$setMusicMode((byte)0);
         }
      });
      DANCERS.clear();
   }

   private static final class DanceState {
      private final Wolf wolf;
      private long lastSeen;
      private long nextChange;
      private byte mode;

      private DanceState(Wolf wolf, long lastSeen, long nextChange, byte mode) {
         this.wolf = wolf;
         this.lastSeen = lastSeen;
         this.nextChange = nextChange;
         this.mode = mode;
      }
   }
}
