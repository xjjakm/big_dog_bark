package org.chxjj.bigdogbark.network;

import org.chxjj.bigdogbark.BigDogBarkMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record WolfVisualEventPayload(int entityId, byte event) implements CustomPacketPayload {
   public static final byte TAME_FAIL = 1;
   public static final byte TAME_SUCCESS = 2;
   public static final byte DOG_BITE = 3;
   public static final Type<WolfVisualEventPayload> TYPE = new Type(BigDogBarkMod.id("wolf_visual_event"));
   public static final StreamCodec<FriendlyByteBuf, WolfVisualEventPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, WolfVisualEventPayload::entityId, ByteBufCodecs.BYTE, WolfVisualEventPayload::event, WolfVisualEventPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
