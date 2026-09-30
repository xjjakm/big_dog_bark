package com.tudouweixiaozhe.bigdogbark.mixin;

import com.tudouweixiaozhe.bigdogbark.anvil.AnvilSoundController;
import com.tudouweixiaozhe.bigdogbark.enchantment.BigDogBarkEnchantment;
import com.tudouweixiaozhe.bigdogbark.enchantment.NoBarkCurseEnchantment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
   @Inject(method = "onTake", at = @At("HEAD"))
   private void bigDogBark$beginCustomSound(Player player, ItemStack output, CallbackInfo ci) {
      AnvilSoundController.end();
      AnvilMenu menu = (AnvilMenu)this;
      ItemStack input = menu.getSlot(0).getItem();
      boolean addedBigDogBark = BigDogBarkEnchantment.has(output) && !BigDogBarkEnchantment.has(input);
      boolean addedNoBarkCurse = NoBarkCurseEnchantment.has(output) && !NoBarkCurseEnchantment.has(input);
      if (player instanceof ServerPlayer serverPlayer && !output.is(Items.ENCHANTED_BOOK) && (addedBigDogBark || addedNoBarkCurse)) {
         AnvilSoundController.begin(serverPlayer);
      }
   }

   @Inject(method = "onTake", at = @At("RETURN"))
   private void bigDogBark$finishCustomSound(Player player, ItemStack output, CallbackInfo ci) {
      AnvilSoundController.end();
   }
}
