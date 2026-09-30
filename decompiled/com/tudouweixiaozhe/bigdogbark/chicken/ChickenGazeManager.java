package com.tudouweixiaozhe.bigdogbark.chicken;

import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;

public final class ChickenGazeManager {
   private static final float TURN_SPEED = 6.0F;
   private static final Map<UUID, ChickenGazeManager.GazeState> STATES = new HashMap<>();

   private ChickenGazeManager() {
   }

   public static void tick(Chicken chicken) {
      if (chicken.level() instanceof ServerLevel level && chicken.isAlive()) {
         ChickenGazeManager.GazeState state = STATES.get(chicken.getUUID());
         if (state == null) {
            ServerPlayer player = findLookingPlayer(level, chicken);
            if (player == null) {
               return;
            }

            state = new ChickenGazeManager.GazeState(player.getUUID(), ChickenGazeManager.Phase.TURNING, 0L, 0.0F);
            STATES.put(chicken.getUUID(), state);
         }

         ServerPlayer player = level.getServer().getPlayerList().getPlayer(state.playerId);
         if (player != null && player.level() == level && !(player.distanceToSqr(chicken) > 1024.0)) {
            chicken.getNavigation().stop();
            if (state.phase == ChickenGazeManager.Phase.TURNING) {
               float wanted = yawTo(chicken, player.getX(), player.getZ());
               face(chicken, player, wanted);
               if (Math.abs(Mth.wrapDegrees(wanted - chicken.getYRot())) <= 4.0F) {
                  ModSounds.play(level, chicken.position(), ModSounds.DING_DONG_CHICK);
                  state.phase = ChickenGazeManager.Phase.PLAYING;
                  state.until = level.getGameTime() + ModSounds.durationTicks(ModSounds.DING_DONG_CHICK);
               }
            } else if (state.phase == ChickenGazeManager.Phase.PLAYING) {
               face(chicken, player, yawTo(chicken, player.getX(), player.getZ()));
               if (level.getGameTime() >= state.until) {
                  state.awayYaw = Mth.wrapDegrees(chicken.getYRot() + 160.0F);
                  state.phase = ChickenGazeManager.Phase.TURNING_AWAY;
               }
            } else if (state.phase == ChickenGazeManager.Phase.TURNING_AWAY) {
               rotate(chicken, state.awayYaw);
               if (Math.abs(Mth.wrapDegrees(state.awayYaw - chicken.getYRot())) <= 4.0F) {
                  state.phase = ChickenGazeManager.Phase.WAITING_FOR_RELEASE;
               }
            } else if (findLookingPlayer(level, chicken) == null) {
               STATES.remove(chicken.getUUID());
            } else {
               rotate(chicken, state.awayYaw);
            }
         } else {
            STATES.remove(chicken.getUUID());
         }
      }
   }

   private static ServerPlayer findLookingPlayer(ServerLevel level, Chicken chicken) {
      ServerPlayer nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (ServerPlayer player : level.players()) {
         double distance = player.distanceToSqr(chicken);
         if (!(distance > 1024.0)
            && !(distance >= nearestDistance)
            && ProjectileUtil.getHitResultOnViewVector(player, entity -> entity == chicken, 32.0) instanceof EntityHitResult entityHit
            && entityHit.getEntity() == chicken) {
            nearest = player;
            nearestDistance = distance;
         }
      }

      return nearest;
   }

   private static void face(Chicken chicken, ServerPlayer player, float wantedYaw) {
      chicken.getLookControl().setLookAt(player, 6.0F, 30.0F);
      rotate(chicken, wantedYaw);
   }

   private static void rotate(Chicken chicken, float wantedYaw) {
      float change = Mth.clamp(Mth.wrapDegrees(wantedYaw - chicken.getYRot()), -6.0F, 6.0F);
      float next = chicken.getYRot() + change;
      chicken.setYRot(next);
      chicken.setYBodyRot(next);
      chicken.setYHeadRot(next);
   }

   private static float yawTo(Chicken chicken, double x, double z) {
      return (float)(Mth.atan2(z - chicken.getZ(), x - chicken.getX()) * 180.0 / Math.PI) - 90.0F;
   }

   public static void clearAll() {
      STATES.clear();
   }

   private static final class GazeState {
      private final UUID playerId;
      private ChickenGazeManager.Phase phase;
      private long until;
      private float awayYaw;

      private GazeState(UUID playerId, ChickenGazeManager.Phase phase, long until, float awayYaw) {
         this.playerId = playerId;
         this.phase = phase;
         this.until = until;
         this.awayYaw = awayYaw;
      }
   }

   private enum Phase {
      TURNING,
      PLAYING,
      TURNING_AWAY,
      WAITING_FOR_RELEASE;
   }
}
