package net.lukasllll.lukas_nutrients.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record HeatedCraftingRecipesConfig(int configVersion, boolean rawFoodGivesLessNutrients, ArrayList<String> recipeIDs){
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/heated_crafting_recipes.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;

    public static HeatedCraftingRecipesConfig DATA = null;

    public Set<RecipeType<?>> getHeatedRecipeTypes() {
        HashSet<RecipeType<?>> out = new HashSet<>();
        for(String key : recipeIDs) {
            out.add(ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation(key)));
        }
        return out;
    }

    private static boolean validate(HeatedCraftingRecipesConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, HeatedCraftingRecipesConfig.class, HeatedCraftingRecipesConfig::validate, HeatedCraftingRecipesConfig::getDefaultEdibleBlocks);
    }

    private static HeatedCraftingRecipesConfig getDefaultEdibleBlocks() {
        ArrayList<String> recipeIDs = new ArrayList<>();

        ///minecraft
        recipeIDs.add(ForgeRegistries.RECIPE_TYPES.getKey(RecipeType.SMELTING).toString());
        recipeIDs.add(ForgeRegistries.RECIPE_TYPES.getKey(RecipeType.BLASTING).toString());
        recipeIDs.add(ForgeRegistries.RECIPE_TYPES.getKey(RecipeType.SMOKING).toString());
        recipeIDs.add(ForgeRegistries.RECIPE_TYPES.getKey(RecipeType.CAMPFIRE_COOKING).toString());

        //farmers delight
        String farmersdelightNamespace = "farmersdelight";
        recipeIDs.add(farmersdelightNamespace + ":cooking");

        return new HeatedCraftingRecipesConfig(CURRENT_CONFIG_VERSION, false, recipeIDs);
    }
}
