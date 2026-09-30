package com.tudouweixiaozhe.bigdogbark.mining;

import com.tudouweixiaozhe.bigdogbark.debug.DebugManager;
import com.tudouweixiaozhe.bigdogbark.debug.ProbabilitySettings;
import com.tudouweixiaozhe.bigdogbark.enchantment.BigDogBarkEnchantment;
import com.tudouweixiaozhe.bigdogbark.enchantment.NoBarkCurseEnchantment;
import com.tudouweixiaozhe.bigdogbark.sound.ModSounds;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class MiningStateManager {
   private static final Map<UUID, MiningState> STATES = new ConcurrentHashMap<>();

   private MiningStateManager() {
   }

   public static void onAction(ServerLevel level, ServerPlayer player, BlockPos pos, Action action, GameType gameType) {
      if (action == Action.ABORT_DESTROY_BLOCK) {
         cancel(player, true);
      } else if (action == Action.START_DESTROY_BLOCK) {
         start(level, player, pos, gameType);
      }
   }

   private static void start(ServerLevel level, ServerPlayer player, BlockPos pos, GameType gameType) {
      cancel(player, STATES.containsKey(player.getUUID()));
      ItemStack tool = player.getMainHandItem();
      boolean cursed = NoBarkCurseEnchantment.has(level, tool);
      if (cursed || BigDogBarkEnchantment.has(level, tool)) {
         if (player.mayInteract(level, pos) && !player.blockActionRestricted(level, pos, gameType)) {
            BlockState block = level.getBlockState(pos);
            if (!block.isAir()) {
               float perTick = block.getDestroyProgress(player, level, pos);
               if (perTick > 0.0F && Float.isFinite(perTick)) {
                  boolean instant = gameType.isCreative() || perTick >= 1.0F;
                  int estimatedTicks = Math.max(1, (int)Math.ceil(1.0 / perTick));
                  int soundCount = Math.max(1, (int)Math.ceil(estimatedTicks / 6.0));
                  MiningState state = new MiningState(
                     level.dimension(),
                     pos.immutable(),
                     block,
                     player.getInventory().getSelectedSlot(),
                     tool.copy(),
                     estimatedTicks,
                     soundCount,
                     cursed,
                     instant,
                     level.getGameTime(),
                     0.0F,
                     level.getGameTime()
                  );
                  STATES.put(player.getUUID(), state);
                  DebugManager.send(player, "方块：" + BuiltInRegistries.BLOCK.getKey(block.getBlock()));
                  DebugManager.send(player, "预计挖掘时间：" + estimatedTicks + " Tick");
                  DebugManager.send(player, "预计播放次数：" + (instant ? 0 : soundCount));
                  if (!instant) {
                     state = emitIfDue(level, player, state, Math.min(perTick, 0.9999F));
                     STATES.put(player.getUUID(), state);
                  }
               }
            }
         }
      }
   }

   public static void tick(ServerLevel level, ServerPlayer player) {
      MiningState state = STATES.get(player.getUUID());
      if (state != null) {
         if (state.instant()) {
            if (level.getGameTime() > state.startedAt() && level.getBlockState(state.pos()).equals(state.originalBlock())) {
               cancel(player, false);
            }
         } else {
            ItemStack current = player.getMainHandItem();
            boolean invalid = !player.isAlive()
               || !level.dimension().equals(state.dimension())
               || player.getInventory().getSelectedSlot() != state.selectedSlot()
               || !ItemStack.isSameItemSameComponents(current, state.originalTool())
               || !NoBarkCurseEnchantment.has(level, current) && !BigDogBarkEnchantment.has(level, current)
               || !level.getBlockState(state.pos()).equals(state.originalBlock())
               || player.distanceToSqr(Vec3.atCenterOf(state.pos())) > 64.0
               || player.containerMenu != player.inventoryMenu;
            if (invalid) {
               cancel(player, true);
            } else {
               float increment = state.originalBlock().getDestroyProgress(player, level, state.pos());
               if (increment > 0.0F && Float.isFinite(increment)) {
                  float progress = Math.min(0.9999F, state.progress() + increment);
                  STATES.put(player.getUUID(), emitIfDue(level, player, state, progress));
               } else {
                  cancel(player, true);
               }
            }
         }
      }
   }

   private static MiningState emitIfDue(ServerLevel level, ServerPlayer player, MiningState state, float progress) {
      long now = level.getGameTime();
      if (now < state.nextSoundAt()) {
         return state.withProgress(progress, state.nextSoundAt());
      }

      ModSounds.Selection selection = ModSounds.forMiningProgress(progress);
      ModSounds.play(level, Vec3.atCenterOf(state.pos()), selection.event());
      DebugManager.send(player, String.format("当前挖掘进度：%.0f%%", progress * 100.0F));
      DebugManager.send(player, "播放：" + selection.fileName());
      int interval = Math.clamp(Math.round(8.0F - progress * 5.0F), 3, 8);
      return state.withProgress(progress, now + interval);
   }

   public static void onDestroyed(ServerLevel level, ServerPlayer player, BlockPos pos, boolean success) {
      if (success) {
         MiningState state = STATES.get(player.getUUID());
         boolean eligible = state != null && state.pos().equals(pos);
         if (eligible) {
            STATES.remove(player.getUUID());
         }

         boolean cursed = eligible && state.cursed();
         if (!eligible) {
            ItemStack tool = player.getMainHandItem();
            cursed = NoBarkCurseEnchantment.has(level, tool);
            eligible = cursed || BigDogBarkEnchantment.has(level, tool);
         }

         if (eligible) {
            ModSounds.play(level, Vec3.atCenterOf(pos), cursed && ProbabilitySettings.rollNoShout(player.getRandom()) ? ModSounds.NO_SHOUT : ModSounds.SHOUT);
            DebugManager.send(player, "播放：shout.ogg");
         }
      }
   }

   public static void cancel(ServerPlayer player, boolean report) {
      if (STATES.remove(player.getUUID()) != null && report) {
         DebugManager.send(player, "挖掘已中断，取消剩余音频");
      }
   }

   public static void clear(UUID playerId) {
      STATES.remove(playerId);
   }

   public static void clearAll() {
      STATES.clear();
   }
}
