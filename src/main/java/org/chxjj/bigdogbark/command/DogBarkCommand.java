package org.chxjj.bigdogbark.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.chxjj.bigdogbark.debug.DebugManager;
import org.chxjj.bigdogbark.debug.ProbabilitySettings;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class DogBarkCommand {
   private DogBarkCommand() {
   }

   public static void register() {
      CommandRegistrationCallback.EVENT
         .register(
            (CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> dispatcher.register(
               (LiteralArgumentBuilder)Commands.literal("dogbark")
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug").then(Commands.literal("on").executes(context -> {
                           DebugManager.set(((CommandSourceStack)context.getSource()).getPlayerOrException(), true);
                           return 1;
                        }))).then(Commands.literal("off").executes(context -> {
                           DebugManager.set(((CommandSourceStack)context.getSource()).getPlayerOrException(), false);
                           return 1;
                        })))
                        .then(
                           ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("probability")
                                          .then(Commands.literal("show").executes(context -> {
                                             ((CommandSourceStack)context.getSource())
                                                .sendSuccess(() -> Component.literal("[大狗叫] " + ProbabilitySettings.describe()), false);
                                             return 1;
                                          })))
                                       .then(Commands.literal("reset").executes(context -> {
                                          ProbabilitySettings.reset();
                                          ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("[大狗叫] 概率已恢复默认值"), false);
                                          return 1;
                                       })))
                                    .then(
                                       Commands.literal("noshout").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(context -> {
                                          int percent = IntegerArgumentType.getInteger(context, "percent");
                                          ProbabilitySettings.setNoShoutPercent(percent);
                                          ((CommandSourceStack)context.getSource())
                                             .sendSuccess(() -> Component.literal("[大狗叫] noshout 概率设为 " + percent + "%"), false);
                                          return 1;
                                       }))
                                    ))
                                 .then(Commands.literal("tame").then(Commands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(context -> {
                                    int percent = IntegerArgumentType.getInteger(context, "percent");
                                    ProbabilitySettings.setTamePercent(percent);
                                    ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("[大狗叫] 驯服成功概率设为 " + percent + "%"), false);
                                    return 1;
                                 }))))
                              .then(
                                 Commands.literal("egg")
                                    .then(
                                       Commands.argument("zero", IntegerArgumentType.integer(0, 100))
                                          .then(
                                             Commands.argument("one", IntegerArgumentType.integer(0, 100))
                                                .then(
                                                   Commands.argument("two", IntegerArgumentType.integer(0, 100))
                                                      .then(
                                                         Commands.argument("three", IntegerArgumentType.integer(0, 100))
                                                            .then(
                                                               Commands.argument("four", IntegerArgumentType.integer(0, 100))
                                                                  .executes(context -> setEggProbabilities(context))
                                                            )
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
         );
   }

   private static int setEggProbabilities(CommandContext<CommandSourceStack> context) {
      int zero = IntegerArgumentType.getInteger(context, "zero");
      int one = IntegerArgumentType.getInteger(context, "one");
      int two = IntegerArgumentType.getInteger(context, "two");
      int three = IntegerArgumentType.getInteger(context, "three");
      int four = IntegerArgumentType.getInteger(context, "four");
      if (!ProbabilitySettings.setEgg(zero, one, two, three, four)) {
         ((CommandSourceStack)context.getSource()).sendFailure(Component.literal("[大狗叫] 五个鸡蛋概率之和必须正好等于 100"));
         return 0;
      } else {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("[大狗叫] 鸡蛋概率已更新：" + ProbabilitySettings.describe()), false);
         return 1;
      }
   }
}
