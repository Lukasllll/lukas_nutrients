package net.lukasllll.lukas_nutrients.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.lukasllll.lukas_nutrients.nutrients.FoodGroup;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class FoodGroupSuggestionProvider implements SuggestionProvider {

    private static FoodGroupSuggestionProvider provider;

    public static SuggestionProvider<CommandSourceStack> getProvider() {
        if(provider==null) provider = new FoodGroupSuggestionProvider();
        return provider;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {

        FoodGroup[] Groups = FoodGroup.getFoodGroups();
        for(int i=0; i<Groups.length; i++) {
            builder.suggest(Groups[i].getID());
        }

        return builder.buildFuture();
    }
}
