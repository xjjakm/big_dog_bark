package org.chxjj.bigdogbark.item;

import org.chxjj.bigdogbark.BigDogBarkMod;
import org.chxjj.bigdogbark.enchantment.BigDogBarkEnchantment;
import org.chxjj.bigdogbark.enchantment.NoBarkCurseEnchantment;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ModItemGroups {
   private static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), BigDogBarkMod.id("big_dog_bark"));
   private static final CreativeModeTab TAB = FabricCreativeModeTab.builder()
      .title(Component.translatable("itemGroup.big_dog_bark"))
      .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
      .displayItems((parameters, output) -> {
         ItemStack bigDogBook = BigDogBarkEnchantment.createBook(parameters.holders());
         if (bigDogBook != null) {
            output.accept(bigDogBook);
         }
         ItemStack noBarkBook = NoBarkCurseEnchantment.createBook(parameters.holders());
         if (noBarkBook != null) {
            output.accept(noBarkBook);
         }
         output.accept(ModItems.EH_BIG_DOG_DISC);
      })
      .build();

   private ModItemGroups() {
   }

   public static void register() {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, TAB);
   }
}
