package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrients;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public static void addNutrientProperties() {
        ArrayList<String> justGrains = new ArrayList<>();
        justGrains.add("grains");
        ArrayList<String> justFruits = new ArrayList<>();
        justFruits.add("fruits");
        ArrayList<String> justVegetables = new ArrayList<>();
        justVegetables.add("vegetables");
        ArrayList<String> justProteins = new ArrayList<>();
        justProteins.add("proteins");
        ArrayList<String> justSugars = new ArrayList<>();
        justSugars.add("sugars");

        addNutrientPropertiesByIDs(Items.APPLE, justFruits, 0.9);
        addNutrientPropertiesByIDs(Items.CHORUS_FRUIT, justFruits, 0.4);
        addNutrientPropertiesByIDs(Items.MELON_SLICE, justFruits, 0.9);
        addNutrientPropertiesByIDs(Items.PUMPKIN, justFruits, 1.0);
        addNutrientPropertiesByIDs(Items.SWEET_BERRIES, justFruits, 0.9);
        addNutrientPropertiesByIDs(Items.GLOW_BERRIES, justFruits, 0.9);
        addNutrientPropertiesByIDs(Items.ENCHANTED_GOLDEN_APPLE, justFruits, 0.9);

        addNutrientPropertiesByIDs(Items.BREAD, justGrains, 1.0);
        addNutrientPropertiesByIDs(Items.WHEAT, justGrains, ((INutrientPropertiesHaver) Items.BREAD).getFoodNutrientProperties().getNutrientAmount("grains")/3);

        addNutrientPropertiesByIDs(Items.CHICKEN, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.PORKCHOP, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.MUTTON, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.BEEF, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.RABBIT, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.COD, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.SALMON, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.TROPICAL_FISH, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.PUFFERFISH, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.SPIDER_EYE, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.MILK_BUCKET, justProteins, 0.2);
        addNutrientPropertiesByIDs(Items.EGG, justProteins, 0.5);
        addNutrientPropertiesByIDs(Items.ROTTEN_FLESH, justProteins, 0.5);

        addNutrientPropertiesByIDs(Items.CARROT, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.BEETROOT, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.KELP, justVegetables, 0.1);
        addNutrientPropertiesByIDs(Items.POTATO, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.POISONOUS_POTATO, justVegetables, 0.4);
        addNutrientPropertiesByIDs(Items.MUSHROOM_STEW, justVegetables, 1.0);
        addNutrientPropertiesByIDs(Items.RED_MUSHROOM, justVegetables, ((INutrientPropertiesHaver) Items.MUSHROOM_STEW).getFoodNutrientProperties().getNutrientAmount("vegetables")*0.35);
        addNutrientPropertiesByIDs(Items.BROWN_MUSHROOM, justVegetables, ((INutrientPropertiesHaver) Items.MUSHROOM_STEW).getFoodNutrientProperties().getNutrientAmount("vegetables")*0.65);

        addNutrientPropertiesByIDs(Items.HONEY_BOTTLE, justSugars, 0.9);
        //addNutrientPropertiesByIDs(Items.SUGAR, justSugars, 1.0);
        addNutrientPropertiesByIDs(Items.COCOA_BEANS, justSugars, 0.6);
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

                    //double amountModifier = item.isEdible() ? currentItem.isEdible() ? item.getFoodProperties().getSaturationModifier() / currentItem.getFoodProperties().getSaturationModifier() : 1 + item.getFoodProperties().getSaturationModifier() : 1.0;
                    //scaleArray(currentItemNutrientAmounts, currentItemCount * amountModifier);
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
