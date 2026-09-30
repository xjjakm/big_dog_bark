package org.chxjj.bigdogbark.enchantment;

import org.chxjj.bigdogbark.BigDogBarkMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public final class NoBarkCurseEnchantment {
   public static final ResourceKey<Enchantment> KEY = ResourceKey.create(Registries.ENCHANTMENT, BigDogBarkMod.id("no_bark_curse"));

   private NoBarkCurseEnchantment() {
   }

   public static boolean has(ServerLevel level, ItemStack stack) {
      return has(stack);
   }

   public static boolean has(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getEnchantments().keySet().stream().anyMatch(holder -> holder.is(KEY)) : false;
   }

   public static ItemStack createBook(Provider registries) {
      Holder<Enchantment> enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(KEY);
      return EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1));
   }
}
