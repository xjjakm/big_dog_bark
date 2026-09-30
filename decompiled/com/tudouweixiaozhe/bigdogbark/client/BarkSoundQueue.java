package com.tudouweixiaozhe.bigdogbark.client;

import com.tudouweixiaozhe.bigdogbark.network.PlayBarkSoundPayload;
import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BarkSoundQueue {
   private static final Deque<BarkSoundQueue.QueuedSound> SOUND_QUEUE = new ArrayDeque<>();
   private static final List<BarkSoundQueue.ActiveSound> FINALES = new ArrayList<>();
   private static final List<BarkSoundQueue.ActiveSound> TAME_FAILURES = new ArrayList<>();
   private static BarkSoundQueue.ActiveSound current;
   private static SoundEvent currentEvent;

   private BarkSoundQueue() {
   }

   public static void receive(Minecraft client, PlayBarkSoundPayload payload) {
      SoundEvent event = ModSounds.byName(payload.sound());
      if (event != null) {
         BarkSoundQueue.QueuedSound queued = new BarkSoundQueue.QueuedSound(event, payload.x(), payload.y(), payload.z());
         if (event == ModSounds.EH) {
            SoundInstance failure = create(queued);
            client.getSoundManager().play(failure);
            TAME_FAILURES.add(new BarkSoundQueue.ActiveSound(failure, 0));
         } else if (event == ModSounds.BIGDOG) {
            for (BarkSoundQueue.ActiveSound failure : TAME_FAILURES) {
               client.getSoundManager().stop(failure.instance());
            }

            TAME_FAILURES.clear();
            WolfVisualStateManager.clearFailures();
            client.getSoundManager().play(create(queued));
         } else if (ModSounds.isImmediate(event)) {
            client.getSoundManager().play(create(queued));
         } else if (ModSounds.isFinale(event)) {
            SOUND_QUEUE.clear();
            if (current != null) {
               client.getSoundManager().stop(current.instance());
            }

            current = null;
            currentEvent = null;
            SoundInstance finale = create(queued);
            client.getSoundManager().play(finale);
            FINALES.add(new BarkSoundQueue.ActiveSound(finale, 0));
            if (event == ModSounds.SHOUT) {
               WolfVisualStateManager.onShout(finale, new Vec3(payload.x(), payload.y(), payload.z()));
            }
         } else {
            if (!FINALES.isEmpty()) {
               return;
            }

            SOUND_QUEUE.clear();
            if (current != null && currentEvent != event) {
               client.getSoundManager().stop(current.instance());
               current = null;
               currentEvent = null;
            }

            SOUND_QUEUE.addLast(queued);
            startNext(client);
         }
      }
   }

   public static void tick(Minecraft client) {
      for (int i = TAME_FAILURES.size() - 1; i >= 0; i--) {
         BarkSoundQueue.ActiveSound active = TAME_FAILURES.get(i).nextTick();
         if (active.age() > 1 && !client.getSoundManager().isActive(active.instance())) {
            TAME_FAILURES.remove(i);
         } else {
            TAME_FAILURES.set(i, active);
         }
      }

      for (int i = FINALES.size() - 1; i >= 0; i--) {
         BarkSoundQueue.ActiveSound active = FINALES.get(i).nextTick();
         if (active.age() > 1 && !client.getSoundManager().isActive(active.instance())) {
            FINALES.remove(i);
         } else {
            FINALES.set(i, active);
         }
      }

      if (current != null) {
         current = current.nextTick();
         if (current.age() > 1 && !client.getSoundManager().isActive(current.instance())) {
            current = null;
            currentEvent = null;
         }
      }

      startNext(client);
   }

   private static void startNext(Minecraft client) {
      if (current == null && FINALES.isEmpty() && !SOUND_QUEUE.isEmpty()) {
         BarkSoundQueue.QueuedSound queued = SOUND_QUEUE.removeFirst();
         SoundInstance instance = create(queued);
         client.getSoundManager().play(instance);
         current = new BarkSoundQueue.ActiveSound(instance, 0);
         currentEvent = queued.event();
      }
   }

   private static SoundInstance create(BarkSoundQueue.QueuedSound sound) {
      return new SimpleSoundInstance(sound.event(), SoundSource.PLAYERS, 1.0F, 1.0F, RandomSource.create(), sound.x(), sound.y(), sound.z());
   }

   public static void clear(Minecraft client) {
      SOUND_QUEUE.clear();
      if (current != null) {
         client.getSoundManager().stop(current.instance());
      }

      for (BarkSoundQueue.ActiveSound finale : FINALES) {
         client.getSoundManager().stop(finale.instance());
      }

      for (BarkSoundQueue.ActiveSound failure : TAME_FAILURES) {
         client.getSoundManager().stop(failure.instance());
      }

      current = null;
      currentEvent = null;
      FINALES.clear();
      TAME_FAILURES.clear();
   }

   private record ActiveSound(SoundInstance instance, int age) {
      BarkSoundQueue.ActiveSound nextTick() {
         return new BarkSoundQueue.ActiveSound(this.instance, this.age + 1);
      }
   }

   private record QueuedSound(SoundEvent event, double x, double y, double z) {
   }
}
