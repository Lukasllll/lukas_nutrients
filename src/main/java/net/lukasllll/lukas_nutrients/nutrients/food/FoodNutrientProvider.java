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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FoodNutrientProvider {

    private static Map<Item, List<Recipe<?>>> allRecipes;
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
        addNutrientPropertiesByIDs(Items.PUMPKIN, justFruits, 2.0);
        addNutrientPropertiesByIDs(Items.SWEET_BERRIES, justFruits, 0.9);
        addNutrientPropertiesByIDs(Items.GLOW_BERRIES, justFruits, 0.9);

        addNutrientPropertiesByIDs(Items.WHEAT, justGrains, 0.3);

        addNutrientPropertiesByIDs(Items.CHICKEN, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.PORKCHOP, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.MUTTON, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.BEEF, justProteins, 0.9);
        //addNutrientPropertiesByIDs(Items.COOKED_BEEF, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.RABBIT, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.COD, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.SALMON, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.TROPICAL_FISH, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.PUFFERFISH, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.SPIDER_EYE, justProteins, 0.9);
        addNutrientPropertiesByIDs(Items.MILK_BUCKET, justProteins, 1.0);
        addNutrientPropertiesByIDs(Items.EGG, justProteins, 0.8);

        addNutrientPropertiesByIDs(Items.CARROT, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.BEETROOT, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.RED_MUSHROOM, justVegetables, 0.4);
        addNutrientPropertiesByIDs(Items.BROWN_MUSHROOM, justVegetables, 0.7);
        addNutrientPropertiesByIDs(Items.KELP, justVegetables, 0.2);
        addNutrientPropertiesByIDs(Items.POTATO, justVegetables, 0.9);
        addNutrientPropertiesByIDs(Items.POISONOUS_POTATO, justVegetables, 0.4);

        addNutrientPropertiesByIDs(Items.HONEY_BOTTLE, justSugars, 0.9);
        //addNutrientPropertiesByIDs(Items.SUGAR, justSugars, 0.6);
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
        double[] largestNutrientAmounts_0 = null;
        double largestTotalNutrientAmounts_0 = 0;
        for(Recipe<?> currentRecipe: recipes) {
            int outputStackCount = currentRecipe.getResultItem().getCount();
            double[] largestNutrientAmounts_1 = new double[NutrientGroup.getNutrientGroups().length];
            double largestTotalNutrientAmounts_1 = 0;
            //for each ingredient
            for(Ingredient ingredient: currentRecipe.getIngredients()) {
                //find the largest amount of nutrients of that ingredient
                double[] largestNutrientAmounts_2 = new double[NutrientGroup.getNutrientGroups().length];
                double largestTotalNutrientAmounts_2 = 0;
                ItemStack[] ingredientStacks = ingredient.getItems();
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
                    double[] tempAmounts = cloneArray(((INutrientPropertiesHaver) currentItem).getFoodNutrientProperties().getNutrientAmounts());
                    double tempTotal = 0;

                    //calculate how many nutrients this item would contribute to the new item

                    double amountModifier = item.isEdible() ? currentItem.isEdible() ? item.getFoodProperties().getSaturationModifier() / currentItem.getFoodProperties().getSaturationModifier() : 1 + item.getFoodProperties().getSaturationModifier() : 1.0;

                    for(int j=0; j<tempAmounts.length; j++) {
                        tempAmounts[j] *= currentItemCount * amountModifier;
                        tempTotal += tempAmounts[j];
                    }
                    if(tempTotal > largestTotalNutrientAmounts_2) {
                        largestNutrientAmounts_2 = tempAmounts;
                        largestTotalNutrientAmounts_2 = tempTotal;
                    }

                }

                addArrays(largestNutrientAmounts_1, largestNutrientAmounts_2);
                largestTotalNutrientAmounts_1 += largestTotalNutrientAmounts_2;
            }
            for(int j=0; j<largestNutrientAmounts_1.length; j++) {
                largestNutrientAmounts_1[j] /= outputStackCount;
            }
            largestTotalNutrientAmounts_1 /= outputStackCount;
            if(largestTotalNutrientAmounts_1 > largestTotalNutrientAmounts_0) {
                largestNutrientAmounts_0 = largestNutrientAmounts_1;
                largestTotalNutrientAmounts_0 = largestTotalNutrientAmounts_1;
            }
        }
        if(largestNutrientAmounts_0 == null) {
            assignNoNutrients(item);
        } else {
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(largestNutrientAmounts_0, isIngredient));
        }
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
