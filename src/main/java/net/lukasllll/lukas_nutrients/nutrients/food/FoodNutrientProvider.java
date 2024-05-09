package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.config.BaseNutrientsConfig;
import net.lukasllll.lukas_nutrients.config.EdibleBlocksConfig;
import net.lukasllll.lukas_nutrients.config.HeatedCraftingRecipesConfig;
import net.lukasllll.lukas_nutrients.integration.IntegrationHelper;
import net.lukasllll.lukas_nutrients.integration.farmersdelight.FarmersDelightFoodNutrientProvider;
import net.lukasllll.lukas_nutrients.integration.nethersdelight.NethersDelightFoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.player.PlayerNutrients;
import net.lukasllll.lukas_nutrients.util.INutrientPropertiesHaver;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FoodNutrientProvider {
    //this class provides (almost) every edible item (and some non-edible items) with NutrientProperties
    //the addNutrientProperties() method is called in LukasNutrients.commonSetup() and provides a selection
    //of items with pre-determined NutrientProperties.
    //The assignUnassignedItems() method is called in ModEvents.onRecipesUpdated() and assigns every edible
    //item that doesn't already have NutrientProperties some based on how they are crafted.
    private static Set<RecipeType<?>> heatedRecipeTypes;
    private static Map<Item, List<Recipe<?>>> allRecipes;
    private static Map<Item, Recipe<?>> smokerRecipesByInput;
    private static List<Item> currentlyWorkingOn = new ArrayList<>();

    public static void getFromConfig() {
        if(BaseNutrientsConfig.DATA == null || HeatedCraftingRecipesConfig.DATA == null) return;

        for(String key : BaseNutrientsConfig.DATA.baseNutrients().keySet()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));      //key is the full path (namespace:path) of the item e.g. "minecraft:apple"
            if(item == null || item == Items.AIR) continue;

            List<String> entry = BaseNutrientsConfig.DATA.baseNutrients().get(key);

            double nutrientEffectiveness = Double.parseDouble(entry.get(entry.size()-1));       //the last String in entry is always supposed to be the nutrientEffectiveness
            List<String> ids = entry.subList(0, entry.size()-1);                                //the rest of the strings are the nutrient groups assigned to the item

            addNutrientPropertiesByIDs(item, ids, nutrientEffectiveness);
        }

        heatedRecipeTypes = HeatedCraftingRecipesConfig.DATA.getHeatedRecipeTypes();
    }

    /*
    Adds nutrient properties to special blocks (such as cake) from the EdibleBlocksConfig.
    This function must be called, after all items have been assigned nutrientProperties, because it gives the block
    properties based on the properties of its item.
     */
    public static void assignEdibleBlocksFromConfig() {
        if(EdibleBlocksConfig.DATA == null) return;

        for(String key : EdibleBlocksConfig.DATA.edibleBlocks().keySet()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key));        //key is the full path (namespace:path) of the block e.g. minecraft:cake
            if(block == null || block == Blocks.AIR) continue;

            Item blockItem = block.asItem();
            if(blockItem == null || blockItem == Items.AIR) continue;
            //the item probably doesn't have nutrient properties yet, because it probably isn't edible
            if(!((INutrientPropertiesHaver) blockItem).hasFoodNutrientProperties()) {
                assignNutrientsThroughRecipe(blockItem);
            }

            NutrientProperties itemNutrientProperties = ((INutrientPropertiesHaver)blockItem).getFoodNutrientProperties();
            itemNutrientProperties.setPlaceableEdible(true);

            int servings = EdibleBlocksConfig.DATA.edibleBlocks().get(key);
            //if the item isn't directly edible change servings to match that of block to display in tooltip.
            if(!blockItem.isEdible()) {
                itemNutrientProperties.setServings(servings);
            }

            ((INutrientPropertiesHaver) block).setFoodNutrientProperties(itemNutrientProperties.clone());
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
        double[] amounts = new double[NutrientManager.getNutrients().length];
        for(int i=0; i<ids.size(); i++) {
            int arrayIndex = NutrientManager.getNutrientArrayIndex(ids.get(i));
            if(arrayIndex != -1) {
                amounts[arrayIndex] = eachAmount;
            }
        }

        ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(amounts));
    }


    //This function is called by the reload command (commands.NutrientsCommand.reloadConfigs())
    public static void reassignAllItems() {
        unassignAllItems();
        getFromConfig();
        assignUnassignedItems();
    }

    private static void unassignAllItems() {
        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(null);
        }

        Collection<Block> blocks = ForgeRegistries.BLOCKS.getValues();
        for(Block block : blocks) {
            ((INutrientPropertiesHaver) block).setFoodNutrientProperties(null);
        }
    }

    public static void assignUnassignedItems() {

        assignSmokerRecipes();

        if(IntegrationHelper.isFarmersDelightLoaded()) {
            FarmersDelightFoodNutrientProvider.assignUniqueItems();
        }
        if(IntegrationHelper.isNethersDelightLoaded()) {
            NethersDelightFoodNutrientProvider.assignUniqueItems();
        }

        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            if(item.isEdible() && !((INutrientPropertiesHaver) item).hasFoodNutrientProperties()) {
                assignNutrientsThroughRecipe(item);
            }
        }

        assignEdibleBlocksFromConfig();
    }

    public static void assignSmokerRecipes() {
        Collection<Item> items = getSmokerRecipesByInput().keySet();
        for(Item item : items) {
            boolean resultHasNutrients = assignNutrientsToSmokerOutput(item);

            if(!HeatedCraftingRecipesConfig.DATA.rawFoodGivesLessNutrients() && resultHasNutrients) {
                ((INutrientPropertiesHaver) item).setFoodNutrientProperties(
                        ((INutrientPropertiesHaver) getSmokerRecipesByInput().get(item).
                                getResultItem(Minecraft.getInstance().level.registryAccess()).getItem())
                                .getFoodNutrientProperties().clone());
                LukasNutrients.LOGGER.info("Recalculated nutrient values of " + item.toString());
            }
        }
    }

    /**
     * This method assigns nutrients to the result item of a smoker recipe.
     * @param item An item that can be cooked in a smoker.
     * @return returns true, if the item has an associated smoker recipe that uses it as an input, and the result item
     * either already has nutrients or was successfully assigned nutrients. Returns false if either, there is no
     * associated smoker recipe or the result item could not be assigned nutrients.
     */
    public static boolean assignNutrientsToSmokerOutput(Item item) {

        Recipe<?> smokerRecipe = getSmokerRecipesByInput().getOrDefault(item, null);
        if(smokerRecipe == null) return false;
        ItemStack outputStack = smokerRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        Item output = outputStack.getItem();

        if(!output.isEdible()) return false;
        if(((INutrientPropertiesHaver) output).hasFoodNutrientProperties()) return true;

        currentlyWorkingOn.add(output);

        int outputStackCount = outputStack.getCount();
        Ingredient ingredient = smokerRecipe.getIngredients().get(0);

        double[] nutrients = null;
        double largestTotalNutrientAmount = 0;
        double nutrientEffectiveness = 0;

        for(ItemStack currentStack : ingredient.getItems()) {
            Item currentItem = currentStack.getItem();
            int currentItemCount = currentStack.getCount();

            if(!((INutrientPropertiesHaver) currentItem).hasFoodNutrientProperties()) {
                continue;
            }
            //clone the nutrient amount array, so that it can be modified, without modifying the original
            double[] currentItemNutrientAmounts = ((INutrientPropertiesHaver) currentItem).getFoodNutrientProperties().getNutrientAmounts().clone();

            double currentItemTotalNutrientAmount = 0;
            for (double currentItemNutrientAmount : currentItemNutrientAmounts) {
                currentItemTotalNutrientAmount += currentItemNutrientAmount;
            }

            double currentItemNutrientEffectiveness = 0.9;
            FoodProperties currentItemFoodProperties = currentItem.getFoodProperties();
            if(currentItemFoodProperties != null) {
                currentItemNutrientEffectiveness = currentItemTotalNutrientAmount / (currentItemFoodProperties.getNutrition() * (1.0 + currentItemFoodProperties.getSaturationModifier()) * PlayerNutrients.BASE_DECAY_RATE);
            }
            //calculate how many nutrients this item would contribute to the new item
            scaleArray(currentItemNutrientAmounts, currentItemCount);
            currentItemTotalNutrientAmount *= currentItemCount;

            //save these nutrients if they are larger than any previous nutrients
            if(currentItemTotalNutrientAmount > largestTotalNutrientAmount) {
                nutrients = currentItemNutrientAmounts;
                largestTotalNutrientAmount = currentItemTotalNutrientAmount;
                nutrientEffectiveness = currentItemNutrientEffectiveness;
            }
        }

        if(nutrients == null) {
            currentlyWorkingOn.remove(output);
            return false;
        }

        FoodProperties foodProperties = output.getFoodProperties();
        double optimalNutrientAmount = nutrientEffectiveness * foodProperties.getNutrition() * (1.0 + foodProperties.getSaturationModifier()) * PlayerNutrients.BASE_DECAY_RATE;
        double scalingFactor = optimalNutrientAmount / largestTotalNutrientAmount / outputStackCount;
        scaleArray(nutrients, scalingFactor);

        ((INutrientPropertiesHaver) output).setFoodNutrientProperties(new NutrientProperties(nutrients));
        LukasNutrients.LOGGER.info("Added nutrient values to " + output.toString());

        currentlyWorkingOn.remove(output);
        return true;
    }

    public static void assignNutrientsThroughRecipe(Item item) {
        currentlyWorkingOn.add(item);
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
            int outputStackCount = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()).getCount();
            double[] currentRecipeNutrientAmounts = new double[NutrientManager.getNutrients().length]; //this saves the nutrient amount of the currentRecipe
            double currentRecipeTotalNutrientAmounts = 0;
            //for each ingredient
            for(Ingredient ingredient: currentRecipe.getIngredients()) {
                //find the largest amount of nutrients of that ingredient
                double[] largestNutrientAmounts_2 = new double[NutrientManager.getNutrients().length];   // this saves the largest amount of nutrients provided
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
                    } else if(heatedRecipeTypes.contains(currentRecipe.getType()) && getSmokerRecipesByInput().containsKey(currentItem)) {
                        //if the recipe is heated and the ingredient has a smoked variant, use that instead
                        Item smokedItem = getSmokerRecipesByInput().get(currentItem).getResultItem(Minecraft.getInstance().level.registryAccess()).getItem();
                        if(((INutrientPropertiesHaver)smokedItem).hasFoodNutrientProperties()) {
                            currentItem = smokedItem;
                        }
                    }
                    //clone the nutrient amount array, so that it can be modified, without modifying the original
                    double[] currentItemNutrientAmounts = ((INutrientPropertiesHaver) currentItem).getFoodNutrientProperties().getNutrientAmounts().clone();
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
            LukasNutrients.LOGGER.info("Considered, but assigned no nutrients to " + item.toString());
        } else {
            //otherwise the largest amount of nutrients provided by any recipe is added to the item
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(largestNutrientAmounts_0));
            LukasNutrients.LOGGER.info("Added nutrient values to " + item.toString());
        }
        //the item is now no longer being worked on. It now definitely has NutrientProperties.
        currentlyWorkingOn.remove(item);
    }

    public static void assignNoNutrients(Item item) {
        double[] nutrientAmounts = new double[NutrientManager.getNutrients().length];
        boolean isIngredient = !item.isEdible();
        ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(nutrientAmounts));
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
            ItemStack outputStack = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if(outputStack == null || outputStack.getItem() == Items.AIR) {
                continue;
            }
            Item outputItem = outputStack.getItem();

            if(out.get(outputItem) == null || out.get(outputItem) == Items.AIR) {
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
            ItemStack outputStack = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if(outputStack == null || outputStack.getItem() == Items.AIR) {
                continue;
            }
            List<Ingredient> ingredients = currentRecipe.getIngredients();
            for(Ingredient ingredient : ingredients) {
                ItemStack[] stacks = ingredient.getItems();
                for(ItemStack stack : stacks) {
                    Item item = stack.getItem();
                    if(item == null || item == Items.AIR) continue;
                    out.put(item, currentRecipe);
                }
            }

        }

        return out;
    }

    public static void addArrays(double[] base, double[] add) {
        if(base.length != add.length) return;
        for(int i=0; i<base.length; i++) {
            base[i] += add[i];
        }
    }

    public static void scaleArray(double[] base, double amount) {
        for(int i=0; i<base.length; i++) {
            base[i] *= amount;
        }
    }
}
