package org.chxjj.bigdogbark.sound;

import org.chxjj.bigdogbark.BigDogBarkMod;
import org.chxjj.bigdogbark.network.PlayBarkSoundPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

public final class ModSounds {
   public static final SoundEvent XSLOW = create("xslow");
   public static final SoundEvent SLOW = create("slow");
   public static final SoundEvent MID = create("mid");
   public static final SoundEvent FAST = create("fast");
   public static final SoundEvent XFAST = create("xfast");
   public static final SoundEvent XXFAST = create("xxfast");
   public static final SoundEvent XXXFAST = create("xxxfast");
   public static final SoundEvent SHOUT = create("shout");
   public static final SoundEvent EH = create("eh");
   public static final SoundEvent BIGDOG = create("bigdog");
   public static final SoundEvent DING_DONG_CHICK = create("dingdongchick");
   public static final SoundEvent DING = create("ding");
   public static final SoundEvent DONG = create("dong");
   public static final SoundEvent CHICK = create("chick");
   public static final SoundEvent DOUBLE_CHICK = create("doublechick");
   public static final SoundEvent TRIPLE_CHICK = create("triplechick");
   public static final SoundEvent QUADRUPLE_CHICK = create("quadruplechick");
   public static final SoundEvent DOG_ANVIL = create("doganvil");
   public static final SoundEvent NO_SHOUT = create("noshout");
   public static final SoundEvent EGG = create("egg");
   public static final SoundEvent SMALL_SHOUT = create("smallshout");
   public static final SoundEvent MUSIC_DISC_EH_BIG_DOG = create("music_disc.eh_big_dog");

   private ModSounds() {
   }

   private static SoundEvent create(String name) {
      return SoundEvent.createVariableRangeEvent(BigDogBarkMod.id(name));
   }

   public static void register() {
      register("xslow", XSLOW);
      register("slow", SLOW);
      register("mid", MID);
      register("fast", FAST);
      register("xfast", XFAST);
      register("xxfast", XXFAST);
      register("xxxfast", XXXFAST);
      register("shout", SHOUT);
      register("eh", EH);
      register("bigdog", BIGDOG);
      register("dingdongchick", DING_DONG_CHICK);
      register("ding", DING);
      register("dong", DONG);
      register("chick", CHICK);
      register("doublechick", DOUBLE_CHICK);
      register("triplechick", TRIPLE_CHICK);
      register("quadruplechick", QUADRUPLE_CHICK);
      register("doganvil", DOG_ANVIL);
      register("noshout", NO_SHOUT);
      register("egg", EGG);
      register("smallshout", SMALL_SHOUT);
      register("music_disc.eh_big_dog", MUSIC_DISC_EH_BIG_DOG);
   }

   private static void register(String name, SoundEvent event) {
      Registry.register(BuiltInRegistries.SOUND_EVENT, BigDogBarkMod.id(name), event);
   }

   public static void play(ServerLevel level, Vec3 position, SoundEvent event) {
      for (ServerPlayer player : level.players()) {
         if (player.distanceToSqr(position) <= 1024.0) {
            playTo(player, position, event);
         }
      }
   }

   public static void playTo(ServerPlayer player, Vec3 position, SoundEvent event) {
      String sound = BuiltInRegistries.SOUND_EVENT.getKey(event).getPath();
      ServerPlayNetworking.send(player, new PlayBarkSoundPayload(sound, position.x, position.y, position.z));
   }

   public static SoundEvent byName(String name) {
      return switch (name) {
         case "xslow" -> XSLOW;
         case "slow" -> SLOW;
         case "mid" -> MID;
         case "fast" -> FAST;
         case "xfast" -> XFAST;
         case "xxfast" -> XXFAST;
         case "xxxfast" -> XXXFAST;
         case "shout" -> SHOUT;
         case "eh" -> EH;
         case "bigdog" -> BIGDOG;
         case "dingdongchick" -> DING_DONG_CHICK;
         case "ding" -> DING;
         case "dong" -> DONG;
         case "chick" -> CHICK;
         case "doublechick" -> DOUBLE_CHICK;
         case "triplechick" -> TRIPLE_CHICK;
         case "quadruplechick" -> QUADRUPLE_CHICK;
         case "doganvil" -> DOG_ANVIL;
         case "noshout" -> NO_SHOUT;
         case "egg" -> EGG;
         case "smallshout" -> SMALL_SHOUT;
         default -> null;
      };
   }

   public static boolean isFinale(SoundEvent event) {
      return event == SHOUT || event == NO_SHOUT;
   }

   public static boolean isImmediate(SoundEvent event) {
      return event == DOG_ANVIL
         || event == EH
         || event == BIGDOG
         || event == DING_DONG_CHICK
         || event == DING
         || event == DONG
         || event == CHICK
         || event == DOUBLE_CHICK
         || event == TRIPLE_CHICK
         || event == QUADRUPLE_CHICK
         || event == SMALL_SHOUT;
   }

   public static int durationTicks(SoundEvent event) {
      if (event == EH) {
         return 11;
      } else if (event == BIGDOG) {
         return 22;
      } else if (event == DING_DONG_CHICK) {
         return 20;
      } else if (event == DING) {
         return 8;
      } else if (event == DONG) {
         return 6;
      } else if (event == CHICK) {
         return 6;
      } else if (event == DOUBLE_CHICK) {
         return 38;
      } else if (event == TRIPLE_CHICK) {
         return 58;
      } else if (event == QUADRUPLE_CHICK) {
         return 75;
      } else if (event == DOG_ANVIL) {
         return 20;
      } else if (event == NO_SHOUT) {
         return 13;
      } else if (event == EGG) {
         return 80;
      } else {
         return event == SMALL_SHOUT ? 15 : 20;
      }
   }

   public static ModSounds.Selection forHealth(float ratio) {
      if (ratio > 0.85F) {
         return new ModSounds.Selection(XSLOW, "xslow.ogg");
      } else if (ratio > 0.7F) {
         return new ModSounds.Selection(SLOW, "slow.ogg");
      } else if (ratio > 0.55F) {
         return new ModSounds.Selection(MID, "mid.ogg");
      } else if (ratio > 0.4F) {
         return new ModSounds.Selection(FAST, "fast.ogg");
      } else if (ratio > 0.25F) {
         return new ModSounds.Selection(XFAST, "xfast.ogg");
      } else {
         return ratio > 0.1F ? new ModSounds.Selection(XXFAST, "xxfast.ogg") : new ModSounds.Selection(XXXFAST, "xxxfast.ogg");
      }
   }

   public static ModSounds.Selection forMiningProgress(float progress) {
      if (progress < 0.15F) {
         return new ModSounds.Selection(XSLOW, "xslow.ogg");
      } else if (progress < 0.3F) {
         return new ModSounds.Selection(SLOW, "slow.ogg");
      } else if (progress < 0.45F) {
         return new ModSounds.Selection(MID, "mid.ogg");
      } else if (progress < 0.6F) {
         return new ModSounds.Selection(FAST, "fast.ogg");
      } else if (progress < 0.75F) {
         return new ModSounds.Selection(XFAST, "xfast.ogg");
      } else {
         return progress < 0.9F ? new ModSounds.Selection(XXFAST, "xxfast.ogg") : new ModSounds.Selection(XXXFAST, "xxxfast.ogg");
      }
   }

   public record Selection(SoundEvent event, String fileName) {
   }
}
