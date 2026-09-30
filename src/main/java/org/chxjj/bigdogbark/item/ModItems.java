package org.chxjj.bigdogbark.item;

import org.chxjj.bigdogbark.BigDogBarkMod;
import org.chxjj.bigdogbark.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public final class ModItems {
   public static final ResourceKey<JukeboxSong> EH_BIG_DOG_SONG = ResourceKey.create(Registries.JUKEBOX_SONG, BigDogBarkMod.id("eh_big_dog"));
   public static final ResourceKey<Item> EH_BIG_DOG_DISC_KEY = ResourceKey.create(Registries.ITEM, BigDogBarkMod.id("eh_big_dog_music_disc"));
   // 播放机制（levelEvent 1010）依赖 jukebox_song 注册表数字 ID，
   // 所以模组世界里必须用注册表条目（Reference）才能出声；
   // 加入原版服务器时注册表里没有该条目，用代码内置实例兜底，
   // 仅保证物品组件初始化不崩溃（原版服务器上唱片本身不存在，不会真正播放）。
   private static final JukeboxSong FALLBACK_SONG = new JukeboxSong(
      BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.MUSIC_DISC_EH_BIG_DOG),
      Component.translatable("jukebox_song.big_dog_bark.eh_big_dog"),
      96.0F,
      15
   );
   public static final Item EH_BIG_DOG_DISC = (Item)Registry.register(
      BuiltInRegistries.ITEM,
      EH_BIG_DOG_DISC_KEY,
      new Item(new Properties()
         .setId(EH_BIG_DOG_DISC_KEY)
         .stacksTo(1)
         .rarity(Rarity.UNCOMMON)
         .delayedComponent(DataComponents.JUKEBOX_PLAYABLE, context -> new JukeboxPlayable(
            context.lookup(Registries.JUKEBOX_SONG)
               .flatMap(lookup -> lookup.get(EH_BIG_DOG_SONG))
               .map(song -> (Holder<JukeboxSong>)song)
               .orElseGet(() -> Holder.direct(FALLBACK_SONG))
         )))
   );

   private ModItems() {
   }

   public static void register() {
   }
}
