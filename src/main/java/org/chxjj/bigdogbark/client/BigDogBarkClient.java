package org.chxjj.bigdogbark.client;

import org.chxjj.bigdogbark.client.render.WolfPaperRenderer;
import org.chxjj.bigdogbark.network.PlayBarkSoundPayload;
import org.chxjj.bigdogbark.network.WolfVisualEventPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.world.entity.EntityTypes;

public final class BigDogBarkClient implements ClientModInitializer {
   public void onInitializeClient() {
      ClientPlayNetworking.registerGlobalReceiver(
         PlayBarkSoundPayload.TYPE, (payload, context) -> context.client().execute(() -> BarkSoundQueue.receive(context.client(), payload))
      );
      ClientPlayNetworking.registerGlobalReceiver(
         WolfVisualEventPayload.TYPE, (payload, context) -> context.client().execute(() -> WolfVisualStateManager.receive(payload))
      );
      EntityRendererRegistry.register(EntityTypes.WOLF, WolfPaperRenderer::new);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         BarkSoundQueue.tick(client);
         WolfVisualStateManager.tick(client);
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         BarkSoundQueue.clear(client);
         WolfVisualStateManager.clear();
      });
   }
}
