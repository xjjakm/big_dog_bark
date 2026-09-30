package com.tudouweixiaozhe.bigdogbark.network;

import com.tudouweixiaozhe.bigdogbark.BigDogBarkMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record PlayBarkSoundPayload(String sound, double x, double y, double z) implements CustomPacketPayload {
   public static final Type<PlayBarkSoundPayload> TYPE = new Type(BigDogBarkMod.id("play_bark_sound"));
   public static final StreamCodec<FriendlyByteBuf, PlayBarkSoundPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PlayBarkSoundPayload::sound,
      ByteBufCodecs.DOUBLE,
      PlayBarkSoundPayload::x,
      ByteBufCodecs.DOUBLE,
      PlayBarkSoundPayload::y,
      ByteBufCodecs.DOUBLE,
      PlayBarkSoundPayload::z,
      PlayBarkSoundPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
