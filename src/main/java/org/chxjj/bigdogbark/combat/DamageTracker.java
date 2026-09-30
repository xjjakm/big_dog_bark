package org.chxjj.bigdogbark.combat;

import org.chxjj.bigdogbark.debug.DebugManager;
import org.chxjj.bigdogbark.debug.ProbabilitySettings;
import org.chxjj.bigdogbark.enchantment.BigDogBarkEnchantment;
import org.chxjj.bigdogbark.enchantment.NoBarkCurseEnchantment;
import org.chxjj.bigdogbark.sound.ModSounds;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class DamageTracker {
   private static final ThreadLocal<Map<LivingEntity, Deque<DamageTracker.Snapshot>>> ACTIVE = ThreadLocal.withInitial(IdentityHashMap::new);

   private DamageTracker() {
   }

   public static void before(LivingEntity target) {
      ACTIVE.get().computeIfAbsent(target, ignored -> new ArrayDeque<>()).push(new DamageTracker.Snapshot(target.getHealth(), target.getAbsorptionAmount()));
   }

   public static void after(LivingEntity target, ServerLevel level, DamageSource source, boolean accepted) {
      Map<LivingEntity, Deque<DamageTracker.Snapshot>> active = ACTIVE.get();
      Deque<DamageTracker.Snapshot> snapshots = active.get(target);
      if (snapshots != null && !snapshots.isEmpty()) {
         DamageTracker.Snapshot before = snapshots.pop();
         if (snapshots.isEmpty()) {
            active.remove(target);
         }

         if (active.isEmpty()) {
            ACTIVE.remove();
         }

         if (accepted && source.getEntity() instanceof ServerPlayer player) {
            ItemStack weapon = source.getWeaponItem();
            boolean cursed = NoBarkCurseEnchantment.has(weapon);
            boolean bigDogBark = BigDogBarkEnchantment.has(weapon);
            if (!cursed && !bigDogBark) {
               weapon = player.getMainHandItem();
               cursed = NoBarkCurseEnchantment.has(weapon);
               bigDogBark = BigDogBarkEnchantment.has(weapon);
            }

            if (cursed || bigDogBark) {
               float afterHealth = target.getHealth();
               float afterAbsorption = target.getAbsorptionAmount();
               float effectiveDamage = before.health + before.absorption - (afterHealth + afterAbsorption);
               if (!(effectiveDamage <= 1.0E-4F)) {
                  float maxHealth = Math.max(target.getMaxHealth(), 1.0E-4F);
                  if (target.isAlive() && !(afterHealth <= 0.0F)) {
                     float ratio = Math.clamp(afterHealth / maxHealth, 0.0F, 1.0F);
                     ModSounds.Selection selection = ModSounds.forHealth(ratio);
                     ModSounds.play(level, target.position(), selection.event());
                     DebugManager.send(player, "目标：" + target.getName().getString());
                     DebugManager.send(player, String.format("伤害前血量：%.1f/%.1f", before.health, maxHealth));
                     DebugManager.send(player, String.format("伤害后血量：%.1f/%.1f", afterHealth, maxHealth));
                     DebugManager.send(player, String.format("剩余比例：%.0f%%", ratio * 100.0F));
                     DebugManager.send(player, "播放：" + selection.fileName());
                  } else {
                     ModSounds.play(
                        level, target.position(), cursed && ProbabilitySettings.rollNoShout(player.getRandom()) ? ModSounds.NO_SHOUT : ModSounds.SHOUT
                     );
                     DebugManager.send(player, "目标被击杀");
                     DebugManager.send(player, "播放：shout.ogg");
                  }
               }
            }
         }
      }
   }

   private record Snapshot(float health, float absorption) {
   }
}
