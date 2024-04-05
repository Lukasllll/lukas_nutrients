package net.lukasllll.lukas_nutrients.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class NutrientGroupSuggestionProvider implements SuggestionProvider {

    private static NutrientGroupSuggestionProvider provider;

    public static SuggestionProvider<CommandSourceStack> getProvider() {
        if(provider==null) provider = new NutrientGroupSuggestionProvider();
        return provider;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) {
        NutrientGroup[] Groups = NutrientGroup.getNutrientGroups();
        for(NutrientGroup group : Groups) {
            builder.suggest(group.getID());
        }
        return builder.buildFuture();
    }
}
