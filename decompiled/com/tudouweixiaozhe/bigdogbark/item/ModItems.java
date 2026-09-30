package com.tudouweixiaozhe.bigdogbark.item;

import com.tudouweixiaozhe.bigdogbark.BigDogBarkMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;

public final class ModItems {
   public static final ResourceKey<JukeboxSong> EH_BIG_DOG_SONG = ResourceKey.create(Registries.JUKEBOX_SONG, BigDogBarkMod.id("eh_big_dog"));
   public static final ResourceKey<Item> EH_BIG_DOG_DISC_KEY = ResourceKey.create(Registries.ITEM, BigDogBarkMod.id("eh_big_dog_music_disc"));
   public static final Item EH_BIG_DOG_DISC = (Item)Registry.register(
      BuiltInRegistries.ITEM,
      EH_BIG_DOG_DISC_KEY,
      new Item(new Properties().setId(EH_BIG_DOG_DISC_KEY).stacksTo(1).rarity(Rarity.UNCOMMON).jukeboxPlayable(EH_BIG_DOG_SONG))
   );

   private ModItems() {
   }

   public static void register() {
   }
}
