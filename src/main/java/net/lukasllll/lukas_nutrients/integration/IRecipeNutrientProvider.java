package net.lukasllll.lukas_nutrients.integration;

import net.minecraft.world.item.crafting.Recipe;

/**
 * Functional Interface.
 * A IRecipeNutrientProvider should return nutrients based on the ingredients of the given recipe. It was created to be
 * used FoodNutrientProvider::assignNutrientsThroughRecipe.
 * This Interface is important mainly for integration, as some mods crafting recipes might need to be handled in a
 * different way from vanilla crafting recipes (e.g. create mod's ProcessingRecipe).
 * For example implementations see: FoodNutrientProvider::getRecipeNutrients (vanilla-like recipes)
 * or CreateFoodNutrientProvider::getProcessingRecipeNutrients (create mod's processing recipes).
 */
public interface IRecipeNutrientProvider {
    /**
     * Should return nutrients based on the ingredients of the given recipe.
     * @param recipe a crafting recipe
     * @return should return a double array of length equal to the amount of different nutrients.
     *   The contents of the returned array should represent the amounts of the different nutrients, that the ingredients
     *   of the recipe provide.
     *   If appropriate, returned nutrients should be normalized to the count of the resulting item stack.
     */
    double[] getRecipeNutrients(Recipe<?> recipe);
}
