package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.config.BaseNutrientsConfig;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrients;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FoodNutrientProvider {
    //this class provides (almost) every edible item (and some non-edible items) with NutrientProperties
    //the addNutrientProperties() method is called in LukasNutrients.commonSetup() and provides a selection
    //of items with pre-determined NutrientProperties.
    //The assignUnassignedItems() method is called in ModEvents.onRecipesUpdated() and assigns every edible
    //item that doesn't already have NutrientProperties some based on how they are crafted.
    private static Map<Item, List<Recipe<?>>> allRecipes;
    private static Map<Item, Recipe<?>> smokerRecipesByInput;
    private static List<Item> currentlyWorkingOn = new ArrayList<>();

    public static void addNutrientPropertiesFromConfig() {
        if(BaseNutrientsConfig.DATA == null) return;

        for(String key : BaseNutrientsConfig.DATA.baseNutrients.keySet()) {
            Item item = Registry.ITEM.get(new ResourceLocation(key));                           //key is the full path (namespace:path) of the item e.g. "minecraft:apple"
            if(item == null) continue;

            List<String> entry = BaseNutrientsConfig.DATA.baseNutrients.get(key);

            double nutrientEffectiveness = Double.parseDouble(entry.get(entry.size()-1));       //the last String in entry is always supposed to be the nutrientEffectiveness
            List<String> ids = entry.subList(0, entry.size()-1);                                //the rest of the strings are the nutrient groups assigned to the item

            addNutrientPropertiesByIDs(item, ids, nutrientEffectiveness);
        }
    }

    private static void addNutrientPropertiesByIDs(Item item, List<String> ids, double nutrientEffectiveness) {
        int differentNutrientGroups = ids.size();
        double totalFoodValue = -1.0;
        boolean isIngredient;
        double eachAmount = 0;
        FoodProperties foodProperties = item.getFoodProperties();
        if(foodProperties != null) {
            totalFoodValue = ((double) foodProperties.getNutrition()) * (1.0 + (double) foodProperties.getSaturationModifier());
            isIngredient = false;
            eachAmount = nutrientEffectiveness * totalFoodValue * PlayerNutrients.BASE_DECAY_RATE / differentNutrientGroups;
        } else {
            eachAmount = nutrientEffectiveness * 1.0 / differentNutrientGroups;
            isIngredient = true;
        }
        double[] amounts = new double[NutrientGroup.getNutrientGroups().length];
        for(int i=0; i<ids.size(); i++) {
            int arrayIndex = NutrientGroup.getArrayIndex(ids.get(i));
            if(arrayIndex != -1) {
                amounts[arrayIndex] = eachAmount;
            }
        }

        ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(amounts, isIngredient));
    }


    //This function is called by the reload command (commands.NutrientsCommand.reloadConfigs())
    public static void reassignAllItems() {
        unassignAllItems();
        addNutrientPropertiesFromConfig();
        assignUnassignedItems();
    }

    private static void unassignAllItems() {
        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(null);
        }
    }

    public static void assignUnassignedItems() {
        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            if(item.isEdible() && !((INutrientPropertiesHaver) item).hasFoodNutrientProperties()) {
                assignNutrientsThroughRecipe(item);
            }
        }
    }

    private static void assignNutrientsThroughRecipe(Item item) {
        currentlyWorkingOn.add(item);
        LukasNutrients.LOGGER.info("adding recipe for " + item.toString());
        List<Recipe<?>> recipes = getAllRecipes().get(item);
        if(recipes == null) {
            assignNoNutrients(item);
            return;
        }
        boolean isIngredient = !item.isEdible();
        double[] largestNutrientAmounts_0 = null; //this saves the largest amount of nutrients provided by any recipe. If there are no recipes this stays null
        double largestTotalNutrientAmounts_0 = 0;
        //we now loop through each recipe to find the largest amount of nutrients they can provide
        for(Recipe<?> currentRecipe: recipes) {
            int outputStackCount = currentRecipe.getResultItem().getCount();
            double[] currentRecipeNutrientAmounts = new double[NutrientGroup.getNutrientGroups().length]; //this saves the nutrient amount of the currentRecipe
            double currentRecipeTotalNutrientAmounts = 0;
            //for each ingredient
            for(Ingredient ingredient: currentRecipe.getIngredients()) {
                //find the largest amount of nutrients of that ingredient
                double[] largestNutrientAmounts_2 = new double[NutrientGroup.getNutrientGroups().length];   // this saves the largest amount of nutrients provided
                                                                                                            // by any item that can be used for this ingredient
                double largestTotalNutrientAmounts_2 = 0;
                ItemStack[] ingredientStacks = ingredient.getItems();
                //loop through all items that can be used for this ingredient to find the one, that provides the most nutrients
                for(int i=0; i<ingredientStacks.length; i++) {
                    Item currentItem = ingredientStacks[i].getItem();
                    int currentItemCount = ingredientStacks[i].getCount();
                    //if the ingredient item doesn't have nutrient properties assign them new properties through their crafting
                    //recipes, but skip items that are currently being assigned recipes to avoid infinite loops.
                    if(!((INutrientPropertiesHaver) currentItem).hasFoodNutrientProperties()) {
                        if(currentlyWorkingOn.contains(currentItem)) {
                            continue;
                        }
                        assignNutrientsThroughRecipe(currentItem);
                    }
                    //clone the nutrient amount array, so that it can be modified, without modifying the original
                    double[] currentItemNutrientAmounts = cloneArray(((INutrientPropertiesHaver) currentItem).getFoodNutrientProperties().getNutrientAmounts());
                    double currentItemTotalNutrientAmount = 0;

                    //calculate how many nutrients this item would contribute to the new item
                    scaleArray(currentItemNutrientAmounts, currentItemCount);

                    //calculation done

                    for(int j=0; j<currentItemNutrientAmounts.length; j++) {
                        currentItemTotalNutrientAmount += currentItemNutrientAmounts[j];
                    }

                    //save these nutrients if they are larger than any previous nutrients
                    if(currentItemTotalNutrientAmount > largestTotalNutrientAmounts_2) {
                        largestNutrientAmounts_2 = currentItemNutrientAmounts;
                        largestTotalNutrientAmounts_2 = currentItemTotalNutrientAmount;
                    }
                }
                //the largest amount of nutrients for that ingredient has been determined and is now added to the nutrient
                //array for that recipe
                addArrays(currentRecipeNutrientAmounts, largestNutrientAmounts_2);
                currentRecipeTotalNutrientAmounts += largestTotalNutrientAmounts_2;
            }
            //after the largest nutrient amounts from all ingredients have been added together, the nutrient amount is
            //divided by the outputStackCount
            for(int j=0; j<currentRecipeNutrientAmounts.length; j++) {
                currentRecipeNutrientAmounts[j] /= outputStackCount;
            }
            currentRecipeTotalNutrientAmounts /= outputStackCount;
            //if the recipe is a smoker recipe, the nutrientAmounts are scaled by the output food values
            if(currentRecipe.getType() == RecipeType.SMOKING && item.isEdible()) {
                FoodProperties foodProperties = item.getFoodProperties();
                double optimalNutrientAmount = 0.9 * foodProperties.getNutrition() * (1.0 + foodProperties.getSaturationModifier()) * PlayerNutrients.BASE_DECAY_RATE;
                double scalingFactor = optimalNutrientAmount / currentRecipeTotalNutrientAmounts;
                scaleArray(currentRecipeNutrientAmounts, scalingFactor);
                currentRecipeTotalNutrientAmounts = optimalNutrientAmount;
            }

            //then it is determined, if the nutrientAmounts of this recipe is larger than the nutrientAmounts of any previous
            //recipe and are saved if that's the case.
            if(currentRecipeTotalNutrientAmounts > largestTotalNutrientAmounts_0) {
                largestNutrientAmounts_0 = currentRecipeNutrientAmounts;
                largestTotalNutrientAmounts_0 = currentRecipeTotalNutrientAmounts;
            }
        }
        //if no recipe could produce any nutrientAmounts, no nutrients are assigned to this item
        if(largestNutrientAmounts_0 == null) {
            assignNoNutrients(item);
        } else {
            //otherwise the largest amount of nutrients provided by any recipe is added to the item
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(largestNutrientAmounts_0, isIngredient));
        }
        //the item is now no longer being worked on. It now definitely has NutrientProperties.
        currentlyWorkingOn.remove(item);
    }

    private static void assignNoNutrients(Item item) {
        double[] nutrientAmounts = new double[NutrientGroup.getNutrientGroups().length];
        boolean isIngredient = !item.isEdible();
        ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(nutrientAmounts, isIngredient));
    }

    private static Map<Item, List<Recipe<?>>> getAllRecipes() {
        if(allRecipes == null) {
            allRecipes = findAllRecipes();
        }
        return allRecipes;
    }

    private static Map<Item, List<Recipe<?>>> findAllRecipes() {
        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        Map<Item, List<Recipe<?>>> out = new HashMap<>();

        for(Recipe<?> currentRecipe: recipeManager.getRecipes()) {
            ItemStack outputStack = currentRecipe.getResultItem();
            if(outputStack == null) {
                continue;
            }
            Item outputItem = outputStack.getItem();

            if(out.get(outputItem) == null) {
                out.put(outputItem, new ArrayList<>());
            }
            out.get(outputItem).add(currentRecipe);
        }

        return out;
    }

    private static Map<Item, Recipe<?>> getSmokerRecipesByInput() {
        if(smokerRecipesByInput == null) {
            smokerRecipesByInput = findAllSmokerRecipes();
        }
        return smokerRecipesByInput;
    }

    private static Map<Item, Recipe<?>> findAllSmokerRecipes() {
        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        Map<Item, Recipe<?>> out = new HashMap<>();

        for(Recipe<?> currentRecipe: recipeManager.getRecipes()) {
            if(currentRecipe.getType() != RecipeType.SMOKING) continue;
            ItemStack outputStack = currentRecipe.getResultItem();
            if(outputStack == null) {
                continue;
            }
            List<Ingredient> ingredients = currentRecipe.getIngredients();
            for(Ingredient ingredient : ingredients) {
                ItemStack[] stacks = ingredient.getItems();
                for(ItemStack stack : stacks) {
                    Item item = stack.getItem();
                    if(item == null) continue;
                    out.put(item, currentRecipe);
                }
            }

        }

        return out;
    }

    private static double[] cloneArray(double[] in) {
        double[] out = new double[in.length];
        for(int i=0; i<in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    private static void addArrays(double[] base, double[] add) {
        if(base.length != add.length) return;
        for(int i=0; i<base.length; i++) {
            base[i] += add[i];
        }
    }

    private static void scaleArray(double[] base, double amount) {
        for(int i=0; i<base.length; i++) {
            base[i] *= amount;
        }
    }
}
