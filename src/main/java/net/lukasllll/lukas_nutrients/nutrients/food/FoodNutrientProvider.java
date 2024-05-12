package net.lukasllll.lukas_nutrients.nutrients.food;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.*;

public class FoodNutrientProvider {
    public static final Logger LOGGER = LogUtils.getLogger();
    //this class provides (almost) every edible item (and some non-edible items) with NutrientProperties
    //the addNutrientProperties() method is called in LukasNutrients.commonSetup() and provides a selection
    //of items with pre-determined NutrientProperties.
    //The assignUnassignedItems() method is called in ModEvents.onRecipesUpdated() and assigns every edible
    //item that doesn't already have NutrientProperties some based on how they are crafted.
    private static Set<RecipeType<?>> heatedRecipeTypes;
    private static Map<Item, List<Recipe<?>>> allRecipesByResult;
    private static Map<Item, Recipe<?>> smokerRecipesByInput;
    private static final Set<Item> currentlyWorkingOn = new HashSet<>();

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
            if(blockItem == Items.AIR) continue;
            //the item probably doesn't have nutrient properties yet, because it probably isn't edible
            if(!((INutrientPropertiesHaver) blockItem).hasFoodNutrientProperties()) {
                assignItemNutrientsThroughRecipes(blockItem);
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
        double eachAmount;
        FoodProperties foodProperties = item.getFoodProperties();
        if(foodProperties != null) {
            double totalFoodValue = ((double) foodProperties.getNutrition()) * (1.0 + (double) foodProperties.getSaturationModifier());
            eachAmount = nutrientEffectiveness * totalFoodValue * PlayerNutrients.BASE_DECAY_RATE / differentNutrientGroups;
        } else {
            eachAmount = nutrientEffectiveness / differentNutrientGroups;
        }
        double[] amounts = new double[NutrientManager.getNutrients().length];
        for (String id : ids) {
            int arrayIndex = NutrientManager.getNutrientArrayIndex(id);
            if (arrayIndex != -1) {
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

    public static void unassignAllItems() {
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

        if(IntegrationHelper.Mods.FARMERSDELIGHT.isLoaded()) {
            FarmersDelightFoodNutrientProvider.assignUniqueItems();
        }
        if(IntegrationHelper.Mods.NETHERSDELIGHT.isLoaded()) {
            NethersDelightFoodNutrientProvider.assignUniqueItems();
        }

        Collection<Item> items = ForgeRegistries.ITEMS.getValues();
        for(Item item : items) {
            if(item.isEdible() && !((INutrientPropertiesHaver) item).hasFoodNutrientProperties()) {
                assignItemNutrientsThroughRecipes(item);
            }
        }

        assignEdibleBlocksFromConfig();
    }

    public static void assignSmokerRecipes() {
        Collection<Item> items = getSmokerRecipesByInput().keySet();
        for(Item item : items) {
            boolean resultHasNutrients = assignNutrientsToSmokerResult(item);

            if(!HeatedCraftingRecipesConfig.DATA.rawFoodGivesLessNutrients() && resultHasNutrients) {
                ((INutrientPropertiesHaver) item).setFoodNutrientProperties(
                        ((INutrientPropertiesHaver) getSmokerRecipesByInput().get(item).
                                getResultItem(Minecraft.getInstance().level.registryAccess()).getItem())
                                .getFoodNutrientProperties().clone());
                FoodNutrientProvider.LOGGER.info("Recalculated nutrient values of " + ForgeRegistries.ITEMS.getKey(item));
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
    public static boolean assignNutrientsToSmokerResult(Item item) {

        Recipe<?> smokerRecipe = getSmokerRecipesByInput().getOrDefault(item, null);
        if(smokerRecipe == null) return false;
        ItemStack resultStack = smokerRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        Item result = resultStack.getItem();

        if(!result.isEdible()) return false;
        if(((INutrientPropertiesHaver) result).hasFoodNutrientProperties()) return true;

        addToCurrentlyWorkingOn(result);

        int resultStackCount = resultStack.getCount();
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
            removeFromCurrentlyWorkingOn(result);
            return false;
        }

        FoodProperties foodProperties = result.getFoodProperties();
        double optimalNutrientAmount = nutrientEffectiveness * foodProperties.getNutrition() * (1.0 + foodProperties.getSaturationModifier()) * PlayerNutrients.BASE_DECAY_RATE;
        double scalingFactor = optimalNutrientAmount / largestTotalNutrientAmount / resultStackCount;
        scaleArray(nutrients, scalingFactor);

        ((INutrientPropertiesHaver) result).setFoodNutrientProperties(new NutrientProperties(nutrients));
        FoodNutrientProvider.LOGGER.info("Added nutrient values to " + ForgeRegistries.ITEMS.getKey(result));

        removeFromCurrentlyWorkingOn(result);
        return true;
    }

    /**
     * Assigns NutrientProperties to a given item based on any registered crafting recipes, that have the given item as
     * a result. If there is no such crafting recipe, an empty nutrient array will be assigned instead.
     * Note that this will overwrite any nutrients the item might currently have.
     * @param item the item to be assigned nutrients.
     */
    public static void assignItemNutrientsThroughRecipes(Item item) {
        List<Recipe<?>> recipes = getAllRecipesByResult().get(item);

        if(recipes == null) {
            assignNoNutrients((INutrientPropertiesHaver) item);
            return;
        }

        addToCurrentlyWorkingOn(item);

        double[] largestNutrientAmounts = null; //this saves the largest amount of nutrients provided by any recipe. If there are no recipes this stays null
        double largestTotalNutrientAmounts = 0;
        //we now loop through each recipe to find the largest amount of nutrients they can provide
        for(Recipe<?> currentRecipe: recipes) {
            //gets the nutrients for the current recipe
            //A modded recipe might have a special INutrientProvider to calculate the nutrients. If no special
            //INutrientProvider was specified (such as for vanilla recipes) FoodNutrientProvider::getRecipeNutrients
            //is used.
            double[] currentRecipeNutrientAmounts = IntegrationHelper.getRecipeNutrientProvider(currentRecipe.getType())
                    .getRecipeNutrients(currentRecipe);

            double currentRecipeTotalNutrientAmounts = 0;
            for (double currentRecipeNutrientAmount : currentRecipeNutrientAmounts) {
                currentRecipeTotalNutrientAmounts += currentRecipeNutrientAmount;
            }

            //then it is determined, if the nutrientAmounts of this recipe is larger than the nutrientAmounts of any previous
            //recipe and are saved if that's the case.
            if(currentRecipeTotalNutrientAmounts > largestTotalNutrientAmounts) {
                largestNutrientAmounts = currentRecipeNutrientAmounts;
                largestTotalNutrientAmounts = currentRecipeTotalNutrientAmounts;
            }
        }
        //if no recipe could produce any nutrientAmounts, no nutrients are assigned to this item
        if(largestNutrientAmounts == null) {
            assignNoNutrients((INutrientPropertiesHaver) item);
            FoodNutrientProvider.LOGGER.info("Considered, but assigned no nutrients to " + ForgeRegistries.ITEMS.getKey(item));
        } else {
            //otherwise the largest amount of nutrients provided by any recipe is added to the item
            ((INutrientPropertiesHaver) item).setFoodNutrientProperties(new NutrientProperties(largestNutrientAmounts));
            FoodNutrientProvider.LOGGER.info("Added nutrient values to " + ForgeRegistries.ITEMS.getKey(item));
        }
        //the item is now no longer being worked on. It now definitely has NutrientProperties.
        removeFromCurrentlyWorkingOn(item);
    }

    /**
     * Implementation of IRecipeNutrientProvider
     * This is the default way of assigning nutrients to a given recipe.
     * @param recipe a vanilla-style crafting recipe (any crafting recipe which properly implements the
     *   net.minecraft.world.item.crafting.Recipe interface without adding anything extra, that might be
     *   important for assigning nutrients).
     * @return Returns a double array of length equal to the amount of different nutrients.
     *   The contents of the returned array represent the amounts of the different nutrients, that the ingredients
     *   of the given recipe provide.
     *   Returned nutrients are normalized to the count of the resulting item stack.
     */
    public static double[] getRecipeNutrients(Recipe<?> recipe) {
        return getRecipeNutrients(recipe, isHeatedRecipe(recipe.getType()));
    }

    /**
     * This does *not* implement IRecipeNutrientProvider!
     * This method is interesting in cases, where only some recipes of a given RecipeType should be considered heated.
     * @param recipe a vanilla-style crafting recipe (any crafting recipe which properly implements the
     *   net.minecraft.world.item.crafting.Recipe interface without adding anything extra, that might be
     *   important for assigning nutrients).
     * @param isHeated whether the recipe is considered heated. If true and an item has a smoker recipe, instead of the
     *   item's own nutrients, that item's smoked variant's nutrients are used to calculate the returned nutrients.
     * @return Returns a double array of length equal to the amount of different nutrients.
     *   The contents of the returned array represent the amounts of the different nutrients, that the ingredients
     *   of the given recipe provide.
     *   Returned nutrients are normalized to the count of the resulting item stack.
     */
    public static double[] getRecipeNutrients(Recipe<?> recipe, boolean isHeated) {
        //The nutrients of the recipe. This is returned at the end of the method
        //For each crafting ingredient, the maximum amount of nutrients that ingredient can provide is added to this array
        double[] recipeNutrientAmounts = new double[NutrientManager.getNutrients().length];

        for(Ingredient currentIngredient: recipe.getIngredients()) {
            //find the largest amount of nutrients provided by any item that can be used for the currentIngredient
            double[] largestItemNutrientAmounts = new double[NutrientManager.getNutrients().length];
            double largestItemTotalNutrientAmount = 0;

            ItemStack[] ingredientStacks = currentIngredient.getItems();
            //loop through all items that can be used for this ingredient to find the one, that provides the most nutrients
            for (ItemStack ingredientStack : ingredientStacks) {
                Item currentItem = ingredientStack.getItem();
                int currentItemCount = ingredientStack.getCount();
                //if the ingredient item doesn't have nutrient properties, assign them new properties through their crafting
                //recipes, but skip items that are currently being assigned recipes to avoid infinite loops.
                if (!((INutrientPropertiesHaver) currentItem).hasFoodNutrientProperties()) {
                    if (isCurrentlyWorkingOn(currentItem)) {
                        continue;
                    }
                    assignItemNutrientsThroughRecipes(currentItem);
                } else if (isHeated && getSmokerRecipesByInput().containsKey(currentItem)) {
                    //if the recipe is heated and the ingredient has a smoked variant, use that instead
                    Item smokedItem = getSmokerRecipesByInput().get(currentItem).getResultItem(Minecraft.getInstance().level.registryAccess()).getItem();
                    if (((INutrientPropertiesHaver) smokedItem).hasFoodNutrientProperties()) {
                        currentItem = smokedItem;
                    }
                }
                //clone the nutrient amount array, so that it can be modified, without modifying the original
                double[] currentItemNutrientAmounts = ((INutrientPropertiesHaver) currentItem).getFoodNutrientProperties().getNutrientAmounts().clone();
                double currentItemTotalNutrientAmount = 0;

                //calculate how many nutrients this item would contribute to the new item
                scaleArray(currentItemNutrientAmounts, currentItemCount);

                for (double currentItemNutrientAmount : currentItemNutrientAmounts) {
                    currentItemTotalNutrientAmount += currentItemNutrientAmount;
                }

                //save these nutrients if they are larger than any previous nutrients
                if (currentItemTotalNutrientAmount > largestItemTotalNutrientAmount) {
                    largestItemNutrientAmounts = currentItemNutrientAmounts;
                    largestItemTotalNutrientAmount = currentItemTotalNutrientAmount;
                }
            }
            //the largest amount of nutrients for that ingredient has been determined and is now added to the nutrient
            //array for that recipe
            addArrays(recipeNutrientAmounts, largestItemNutrientAmounts);
        }
        //after the largest nutrient amounts from all ingredients have been added together, the nutrient amount is
        //divided by the resultStackCount (if applicable)
        int resultStackCount = recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).getCount();
        if(resultStackCount != 0) {
            for (int j = 0; j < recipeNutrientAmounts.length; j++) {
                recipeNutrientAmounts[j] /= resultStackCount;
            }
        }

        return recipeNutrientAmounts;
    }

    public static void assignNoNutrients(INutrientPropertiesHaver nutrientPropertiesHaver) {
        double[] nutrientAmounts = new double[NutrientManager.getNutrients().length];
        (nutrientPropertiesHaver).setFoodNutrientProperties(new NutrientProperties(nutrientAmounts));
    }

    private static Map<Item, List<Recipe<?>>> getAllRecipesByResult() {
        if(allRecipesByResult == null) {
            allRecipesByResult = findAllRecipesByResult();
        }
        return allRecipesByResult;
    }

    private static Map<Item, List<Recipe<?>>> findAllRecipesByResult() {
        RecipeManager recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
        Map<Item, List<Recipe<?>>> out = new HashMap<>();

        for(Recipe<?> currentRecipe: recipeManager.getRecipes()) {
            ItemStack resultStack = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if(resultStack.getItem() == Items.AIR) {
                continue;
            }
            Item resultItem = resultStack.getItem();

            out.computeIfAbsent(resultItem, k -> new ArrayList<>());
            out.get(resultItem).add(currentRecipe);
        }

        return out;
    }

    public static Map<Item, Recipe<?>> getSmokerRecipesByInput() {
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
            ItemStack resultStack = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if(resultStack.getItem() == Items.AIR) {
                continue;
            }
            List<Ingredient> ingredients = currentRecipe.getIngredients();
            for(Ingredient ingredient : ingredients) {
                ItemStack[] stacks = ingredient.getItems();
                for(ItemStack stack : stacks) {
                    Item item = stack.getItem();
                    if(item == Items.AIR) continue;
                    out.put(item, currentRecipe);
                }
            }

        }

        return out;
    }

    public static boolean isHeatedRecipe(RecipeType<?> type) {
        return heatedRecipeTypes.contains(type);
    }

    public static void addToCurrentlyWorkingOn(Item item) {
        currentlyWorkingOn.add(item);
    }

    public static boolean isCurrentlyWorkingOn(Item item) {
        return currentlyWorkingOn.contains(item);
    }

    public static void removeFromCurrentlyWorkingOn(Item item) {
        currentlyWorkingOn.remove(item);
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
