package org.chxjj.bigdogbark.debug;

import net.minecraft.util.RandomSource;

public final class ProbabilitySettings {
   private static final int[] DEFAULT_EGG = new int[]{70, 20, 5, 3, 2};
   private static int[] egg = (int[])DEFAULT_EGG.clone();
   private static int noShoutPercent = 50;
   private static int tamePercent = -1;

   private ProbabilitySettings() {
   }

   public static synchronized boolean setEgg(int zero, int one, int two, int three, int four) {
      int[] values = new int[]{zero, one, two, three, four};
      int total = 0;

      for (int value : values) {
         if (value < 0 || value > 100) {
            return false;
         }

         total += value;
      }

      if (total != 100) {
         return false;
      }

      egg = values;
      return true;
   }

   public static synchronized void setNoShoutPercent(int percent) {
      noShoutPercent = Math.clamp(percent, 0, 100);
   }

   public static synchronized void setTamePercent(int percent) {
      tamePercent = Math.clamp(percent, 0, 100);
   }

   public static synchronized int rollEgg(RandomSource random) {
      int roll = random.nextInt(100);
      int cumulative = 0;

      for (int chicks = 0; chicks < egg.length; chicks++) {
         cumulative += egg[chicks];
         if (roll < cumulative) {
            return chicks;
         }
      }

      return 4;
   }

   public static synchronized boolean rollNoShout(RandomSource random) {
      return random.nextInt(100) < noShoutPercent;
   }

   public static synchronized boolean rollTame(RandomSource random, int vanillaBound) {
      return tamePercent < 0 ? random.nextInt(vanillaBound) == 0 : random.nextInt(100) < tamePercent;
   }

   public static synchronized String describe() {
      String tame = tamePercent < 0 ? "原版 1/3" : tamePercent + "%";
      return String.format("鸡蛋[0只=%d%%, 1只=%d%%, 2只=%d%%, 3只=%d%%, 4只=%d%%]；noshout=%d%%；驯服=%s", egg[0], egg[1], egg[2], egg[3], egg[4], noShoutPercent, tame);
   }

   public static synchronized void reset() {
      egg = (int[])DEFAULT_EGG.clone();
      noShoutPercent = 50;
      tamePercent = -1;
   }
}
