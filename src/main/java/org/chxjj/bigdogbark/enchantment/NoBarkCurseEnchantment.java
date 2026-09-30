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

   // 注册表里没有该附魔时（如客户端进入原版服务器）返回 null，调用方需跳过
   public static @org.jspecify.annotations.Nullable ItemStack createBook(Provider registries) {
      Holder<Enchantment> enchantment = registries.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(KEY)).orElse(null);
      if (enchantment == null) {
         return null;
      }
      return EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1));
   }
}
