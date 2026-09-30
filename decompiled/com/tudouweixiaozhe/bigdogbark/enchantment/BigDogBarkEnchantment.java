package com.tudouweixiaozhe.bigdogbark.enchantment;

import com.tudouweixiaozhe.bigdogbark.BigDogBarkMod;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public final class BigDogBarkEnchantment {
   public static final ResourceKey<Enchantment> KEY = ResourceKey.create(Registries.ENCHANTMENT, BigDogBarkMod.id("big_dog_bark"));

   private BigDogBarkEnchantment() {
   }

   public static boolean has(ServerLevel level, ItemStack stack) {
      return has(stack);
   }

   public static boolean has(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getEnchantments().keySet().stream().anyMatch(holder -> holder.is(KEY)) : false;
   }

   public static ItemStack createBook(Provider registries) {
      Holder<Enchantment> enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(KEY);
      ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1));
      book.set(
         DataComponents.TOOLTIP_DISPLAY,
         TooltipDisplay.DEFAULT.withHidden(DataComponents.ENCHANTMENTS, true).withHidden(DataComponents.STORED_ENCHANTMENTS, true)
      );
      book.set(
         DataComponents.LORE,
         new ItemLore(List.of(Component.literal("大狗叫").withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false))))
      );
      return book;
   }
}
