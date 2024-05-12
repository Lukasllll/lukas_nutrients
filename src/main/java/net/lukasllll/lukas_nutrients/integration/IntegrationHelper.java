package net.lukasllll.lukas_nutrients.integration;

import com.simibubi.create.foundation.utility.Lang;
import net.lukasllll.lukas_nutrients.integration.create.CreateFoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;

public class IntegrationHelper {
    /**
     * Saves RecipeTypes that need special IRecipeNutrientProviders to assign them nutrients. Sometimes needed for
     * integration. See the documentation of IRecipeNutrientProviders for more info.
     * If a IRecipeNutrientProvider is put into outsourcedRecipeNutrientProviders that
     * IRecipeNutrientProvider is used to assign nutrients to Recipes of the type that was used as its key.
     */
    private static final Map<RecipeType<?>, IRecipeNutrientProvider> outsourcedRecipeNutrientProviders = new HashMap<>();

    public static void init() {
        if(Mods.CREATE.isLoaded()) {
            CreateFoodNutrientProvider.init();
        }
    }

    public static void addOutsourcedRecipeNutrientProvider(RecipeType<?> type, IRecipeNutrientProvider provider) {
        outsourcedRecipeNutrientProviders.put(type, provider);
    }

    public static boolean isRecipeTypeOutsourced(RecipeType<?> type) {
        return outsourcedRecipeNutrientProviders.containsKey(type);
    }

    public static IRecipeNutrientProvider getRecipeNutrientProvider(RecipeType<?> type) {
        return outsourcedRecipeNutrientProviders.getOrDefault(type, FoodNutrientProvider::getRecipeNutrients);
    }

    public enum Mods {
        FARMERSDELIGHT,
        NETHERSDELIGHT,
        CREATE;

        private final String id = Lang.asId(this.name());

        public String id() {
            return this.id;
        }

        public boolean isLoaded() {
            return ModList.get().isLoaded(this.id);
        }

    }
}
