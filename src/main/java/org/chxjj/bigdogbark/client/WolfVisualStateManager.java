package org.chxjj.bigdogbark.client;

import org.chxjj.bigdogbark.network.WolfVisualEventPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.phys.Vec3;

public final class WolfVisualStateManager {
   private static final int FAIL_TICKS = 11;
   private static final int SUCCESS_TICKS = 22;
   private static final int BITE_TICKS = 20;
   private static final double SHOUT_RANGE_SQR = 1024.0;
   private static final Map<Integer, WolfVisualStateManager.TimedEvent> FAILURES = new HashMap<>();
   private static final Map<Integer, WolfVisualStateManager.TimedEvent> SUCCESSES = new HashMap<>();
   private static final Map<Integer, WolfVisualStateManager.TimedEvent> BITES = new HashMap<>();
   private static final List<WolfVisualStateManager.ActiveShout> SHOUTS = new ArrayList<>();
   private static long clientTicks;

   private WolfVisualStateManager() {
   }

   public static void receive(WolfVisualEventPayload payload) {
      if (payload.event() == 1) {
         FAILURES.put(payload.entityId(), new WolfVisualStateManager.TimedEvent(clientTicks, 11));
      } else if (payload.event() == 2) {
         FAILURES.remove(payload.entityId());
         SUCCESSES.put(payload.entityId(), new WolfVisualStateManager.TimedEvent(clientTicks, 22));
      } else if (payload.event() == 3) {
         BITES.put(payload.entityId(), new WolfVisualStateManager.TimedEvent(clientTicks, 20));
      }
   }

   public static void onShout(SoundInstance instance, Vec3 source) {
      SHOUTS.add(new WolfVisualStateManager.ActiveShout(instance, source, 0));
   }

   public static void clearFailures() {
      FAILURES.clear();
   }

   public static void tick(Minecraft client) {
      clientTicks++;
      expireTimed(FAILURES, client);
      expireTimed(SUCCESSES, client);
      expireTimed(BITES, client);

      for (int i = SHOUTS.size() - 1; i >= 0; i--) {
         WolfVisualStateManager.ActiveShout shout = SHOUTS.get(i).nextTick();
         if (shout.age() > 1 && !client.getSoundManager().isActive(shout.instance())) {
            SHOUTS.remove(i);
         } else {
            SHOUTS.set(i, shout);
         }
      }
   }

   private static void expireTimed(Map<Integer, WolfVisualStateManager.TimedEvent> events, Minecraft client) {
      events.entrySet().removeIf(entry -> clientTicks >= entry.getValue().endTick() || client.level == null || client.level.getEntity(entry.getKey()) == null);
   }

   public static WolfVisualStateManager.Effects effects(Wolf wolf, float partialTick) {
      WolfVisualStateManager.TimedEvent success = SUCCESSES.get(wolf.getId());
      boolean successActive = success != null && clientTicks < success.endTick();
      WolfVisualStateManager.TimedEvent bite = BITES.get(wolf.getId());
      int biteDuration = wolf.isBaby() ? 15 : 20;
      boolean biteActive = bite != null && clientTicks < bite.startTick() + biteDuration;
      boolean shoutActive = false;

      for (WolfVisualStateManager.ActiveShout shout : SHOUTS) {
         if (wolf.distanceToSqr(shout.source()) <= 1024.0) {
            shoutActive = true;
            break;
         }
      }

      float successJump = 0.0F;
      if (successActive) {
         float age = (float)(clientTicks - success.startTick()) + partialTick;
         if (age < 12.0F) {
            successJump = (float)Math.sin(Math.PI * Math.max(0.0F, age) / 12.0) * 0.32F;
         }
      }

      float failurePitch = 0.0F;
      WolfVisualStateManager.TimedEvent failure = FAILURES.get(wolf.getId());
      if (!successActive && !shoutActive && !biteActive && failure != null && clientTicks < failure.endTick()) {
         float age = (float)(clientTicks - failure.startTick()) + partialTick;
         failurePitch = (float)Math.sin((Math.PI * 2) * Math.max(0.0F, age) / 11.0) * 12.0F;
      }

      return new WolfVisualStateManager.Effects(successActive || shoutActive || biteActive, successJump, failurePitch);
   }

   public static void clear() {
      FAILURES.clear();
      SUCCESSES.clear();
      BITES.clear();
      SHOUTS.clear();
      clientTicks = 0L;
   }

   private record ActiveShout(SoundInstance instance, Vec3 source, int age) {
      WolfVisualStateManager.ActiveShout nextTick() {
         return new WolfVisualStateManager.ActiveShout(this.instance, this.source, this.age + 1);
      }
   }

   public record Effects(boolean barking, float successJump, float failurePitch) {
   }

   private record TimedEvent(long startTick, int duration) {
      long endTick() {
         return this.startTick + this.duration;
      }
   }
}
