package net.lukasllll.lukas_nutrients.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class NutrientSuggestionProvider implements SuggestionProvider {

    private static NutrientSuggestionProvider provider;

    public static SuggestionProvider<CommandSourceStack> getProvider() {
        if(provider==null) provider = new NutrientSuggestionProvider();
        return provider;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) {
        Nutrient[] nutrients = NutrientManager.getNutrients();
        for(Nutrient group : nutrients) {
            builder.suggest(group.getID());
        }
        return builder.buildFuture();
    }
}
