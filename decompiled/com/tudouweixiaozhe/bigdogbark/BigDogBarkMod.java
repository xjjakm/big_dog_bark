package com.tudouweixiaozhe.bigdogbark;

import com.tudouweixiaozhe.bigdogbark.chicken.ChickenEventManager;
import com.tudouweixiaozhe.bigdogbark.chicken.ChickenGazeManager;
import com.tudouweixiaozhe.bigdogbark.command.DogBarkCommand;
import com.tudouweixiaozhe.bigdogbark.debug.DebugManager;
import com.tudouweixiaozhe.bigdogbark.debug.ProbabilitySettings;
import com.tudouweixiaozhe.bigdogbark.item.ModItemGroups;
import com.tudouweixiaozhe.bigdogbark.item.ModItems;
import com.tudouweixiaozhe.bigdogbark.jukebox.JukeboxDogDanceManager;
import com.tudouweixiaozhe.bigdogbark.mining.MiningStateManager;
import com.tudouweixiaozhe.bigdogbark.network.ModNetworking;
import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.minecraft.resources.Identifier;

public final class BigDogBarkMod implements ModInitializer {
   public static final String MOD_ID = "big_dog_bark";

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("big_dog_bark", path);
   }

   public void onInitialize() {
      ModSounds.register();
      ModNetworking.register();
      ModItems.register();
      ModItemGroups.register();
      DogBarkCommand.register();
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         ChickenEventManager.tick(server);
         JukeboxDogDanceManager.finishServerTick(server);
      });
      ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> {
         MiningStateManager.clear(handler.getPlayer().getUUID());
         ChickenEventManager.clear(handler.getPlayer().getUUID());
         DebugManager.clear(handler.getPlayer().getUUID());
      });
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)server -> {
         MiningStateManager.clearAll();
         ChickenEventManager.clearAll();
         ChickenGazeManager.clearAll();
         JukeboxDogDanceManager.clearAll();
         ProbabilitySettings.reset();
         DebugManager.clearAll();
      });
   }
}
