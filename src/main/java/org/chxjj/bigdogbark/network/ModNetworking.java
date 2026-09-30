package org.chxjj.bigdogbark.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
   private ModNetworking() {
   }

   public static void register() {
      PayloadTypeRegistry.clientboundPlay().register(PlayBarkSoundPayload.TYPE, PlayBarkSoundPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(WolfVisualEventPayload.TYPE, WolfVisualEventPayload.CODEC);
   }
}
