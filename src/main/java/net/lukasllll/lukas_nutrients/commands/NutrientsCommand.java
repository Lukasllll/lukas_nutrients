package net.lukasllll.lukas_nutrients.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.lukasllll.lukas_nutrients.config.Config;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrients;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;

import java.util.Collection;
import java.util.List;

public class NutrientsCommand {

    public NutrientsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nutrients").requires((command) -> {
            return command.hasPermission(2);
        }).then(Commands.literal("set").then(Commands.argument("target", EntityArgument.players()).then(Commands.argument("nutrient id", StringArgumentType.string()).suggests(NutrientSuggestionProvider.getProvider()).then(Commands.argument("amount", IntegerArgumentType.integer(0, 24)).executes((command) -> {
            return setNutrients(command.getSource(), EntityArgument.getPlayers(command, "target"), StringArgumentType.getString(command, "nutrient id"), IntegerArgumentType.getInteger(command, "amount"));
        }))))).then(Commands.literal("get").then(Commands.argument("target", EntityArgument.players()).executes((command) -> {
            return getAllNutrients(command.getSource(), EntityArgument.getPlayers(command, "target"));
                }).then(Commands.argument("nutrient id", StringArgumentType.string()).suggests(NutrientSuggestionProvider.getProvider()).executes((command) -> {
            return getNutrients(command.getSource(), EntityArgument.getPlayers(command, "target"), StringArgumentType.getString(command, "nutrient id"));
        })))).then(Commands.literal("list").executes((command) -> {
            return listNutrients(command.getSource());
        })).then(Commands.literal("reload").executes((command) -> {
            return reloadConfigs(command.getSource());
        })));
    }

    private int setNutrients(CommandSourceStack source, Collection<ServerPlayer> players, String nutrientID, int amount) {
        for(ServerPlayer player : players) {
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.setAmount(nutrientID, amount);
                nutrients.updateClient(player);
                NutrientEffects.apply(player);
                logNutrientChange(source, player, nutrients, nutrientID, amount);
            });
        }

        return players.size();
    }

    private static void logNutrientChange(CommandSourceStack source, ServerPlayer player, PlayerNutrients nutrients, String nutrientID, int amount) {
        if (source.getEntity() == player) {
            source.sendSuccess(() -> Component.literal("Set own " + nutrients.getDisplayName(nutrientID) + " to " + amount), true);
        } else {
            if (source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.sendSystemMessage(Component.literal(nutrients.getDisplayName(nutrientID) + " was set to " + amount));
            }

            source.sendSuccess(() -> Component.literal("Set " + nutrients.getDisplayName(nutrientID) + " to " + amount + " for ").append(player.getDisplayName()), true);
        }

    }

    private int getNutrients(CommandSourceStack source, Collection<ServerPlayer> players, String nutrientID) {

        for(ServerPlayer player : players) {
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                double amount = nutrients.getNutrientAmount(nutrientID);
                if(source.getEntity() == player)
                    source.sendSuccess(() -> Component.literal("Own " + nutrients.getDisplayName(nutrientID) + ": " + amount), true);
                else {
                    source.sendSuccess(() -> Component.empty().append(player.getDisplayName()).append(Component.literal(" " + nutrients.getDisplayName(nutrientID) + ": " + amount)), true);
                }
            });
        }

        return players.size();
    }

    private int getAllNutrients(CommandSourceStack source, Collection<ServerPlayer> players) {

        for(ServerPlayer player : players) {
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(playerNutrients -> {
                MutableComponent message = Component.empty().append("[");
                Nutrient[] nutrients = NutrientManager.getNutrients();
                for(int i = 0; i < nutrients.length; i++) {
                    String nutrientID = nutrients[i].getID();
                    String displayName = nutrients[i].getDisplayname();
                    double amount = playerNutrients.getNutrientAmount(nutrientID);
                    message.append(displayName + ": " + amount);
                    if(i < nutrients.length - 1) message.append(", ");
                }
                message.append("]");
                if(source.getEntity() == player)
                    source.sendSuccess(() -> Component.literal("Own nutrients: ").append(message), true);
                else {
                    source.sendSuccess(() -> Component.empty().append(player.getDisplayName()).append(Component.literal(" nutrients: ")).append(message), true);
                }
            });
        }

        return players.size();
    }

    private int listNutrients(CommandSourceStack source) {

        Nutrient[] nutrients = NutrientManager.getNutrients();

        String message = "Food Groups: ";
        for(int i=0; i<nutrients.length; i++) {
            message += nutrients[i].getID();
            if(i!=nutrients.length-1)
                message += ", ";
        }

        String finalMessage = message;
        source.sendSuccess(() -> Component.literal(finalMessage), true);

        return nutrients.length;
    }

    private int reloadConfigs(CommandSourceStack source) {
        Config.loadCommonConfigs();
        FoodNutrientProvider.reassignAllItems();
        source.sendSuccess(() -> Component.literal("Common configs reloaded!"), true);
        //remove all diet effects and recalculate everything.
        source.sendSuccess(() -> Component.literal("Resetting all diet effects..."), true);
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        for(ServerPlayer player : players) {
            NutrientManager.updateClient(player);
            int previousMaxHealth = (int) player.getAttribute(Attributes.MAX_HEALTH).getValue();
            NutrientEffects.removeAll(player);
            player.getCapability(PlayerNutrientProvider.PLAYER_NUTRIENTS).ifPresent(nutrients -> {
                nutrients.reload();
                    });
            NutrientEffects.apply(player, previousMaxHealth);
        }
        source.sendSuccess(() -> Component.literal("Diet effects have been reset for " + players.size() + (players.size() == 1 ? " player" : " players") + "!"), true);

        return 1;
    }

}
