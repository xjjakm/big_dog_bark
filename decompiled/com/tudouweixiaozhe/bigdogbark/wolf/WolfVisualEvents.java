package com.tudouweixiaozhe.bigdogbark.wolf;

import com.tudouweixiaozhe.bigdogbark.network.WolfVisualEventPayload;
import java.util.LinkedHashSet;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.wolf.Wolf;

public final class WolfVisualEvents {
   private WolfVisualEvents() {
   }

   public static void send(Wolf wolf, byte event, ServerPlayer involvedPlayer) {
      Set<ServerPlayer> viewers = new LinkedHashSet<>(PlayerLookup.tracking(wolf));
      if (involvedPlayer != null) {
         viewers.add(involvedPlayer);
      }

      WolfVisualEventPayload payload = new WolfVisualEventPayload(wolf.getId(), event);

      for (ServerPlayer viewer : viewers) {
         ServerPlayNetworking.send(viewer, payload);
      }
   }
}
