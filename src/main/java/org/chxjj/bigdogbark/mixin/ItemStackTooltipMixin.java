package org.chxjj.bigdogbark.mixin;

import org.chxjj.bigdogbark.enchantment.BigDogBarkEnchantment;
import org.chxjj.bigdogbark.enchantment.NoBarkCurseEnchantment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
   @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
   private void bigDogBark$strikeOverriddenEnchantment(TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
      ItemStack stack = (ItemStack)(Object)this;
      if (BigDogBarkEnchantment.has(stack) && NoBarkCurseEnchantment.has(stack)) {
         String bigDogName = Component.translatable("enchantment.big_dog_bark.big_dog_bark").getString();
         List<Component> changed = new ArrayList<>((Collection<? extends Component>)cir.getReturnValue());

         for (int i = 0; i < changed.size(); i++) {
            Component line = changed.get(i);
            if (line.getString().startsWith(bigDogName)) {
               changed.set(i, line.copy().withStyle(ChatFormatting.STRIKETHROUGH));
            }
         }

         cir.setReturnValue(changed);
      }
   }
}
