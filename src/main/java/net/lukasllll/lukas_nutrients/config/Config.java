package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config{
    /*
    This is helper class to help load configs.
     */

    public static final String FOLDER_FILE_PATH = FMLPaths.CONFIGDIR.get().toString()+"/"+ LukasNutrients.MOD_ID;

    public static void loadCommonConfigs() {
        createFolder();
        NutrientsConfig.read();
        NutrientManager.getFromConfig();
        NutrientEffectsConfig.read();
        NutrientEffects.getFromConfig();
        BaseNutrientsConfig.read();
        EdibleBlocksConfig.read();
        HeatedCraftingRecipesConfig.read();
        FoodNutrientProvider.getFromConfig();
    }

    public static void loadClientConfigs() {
        EffectIconsConfig.read();
    }

    private static void createFolder() {
        File configFolder = new File(FOLDER_FILE_PATH);
        if(!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    public static <T> T readConfigFile(String path, Class<T> typeToken, ConfigValidator<T> configValidator, DefaultConfigProvider<T> defaultProvider) {
        File file = new File(path);
        if(!file.exists()) {
            LukasNutrients.LOGGER.debug(file.getAbsolutePath() + " doesn't exist!");
            return createConfigFile(file.getAbsolutePath(), defaultProvider);
        }

        Gson gson = new Gson();

        T out;

        try(FileReader reader = new FileReader(file)) {
            out = gson.fromJson(reader, typeToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(!configValidator.validate(out)) {
            LukasNutrients.LOGGER.warn("Config at " + file.getAbsolutePath() + " is not valid. Loading defaults instead.");
            return defaultProvider.getDefault();
        }

        LukasNutrients.LOGGER.debug("Successfully read " + file.getAbsolutePath() + ".");
        return out;
    }

    private static <T> T createConfigFile(String path, DefaultConfigProvider<T> defaultProvider) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        T defaultConfig = defaultProvider.getDefault();

        try(FileWriter writer = new FileWriter(path)) {
            gson.toJson(defaultConfig, writer);
            LukasNutrients.LOGGER.debug("Successfully created " + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return defaultConfig;
    }

    public static boolean checkConfigVersion(int min, int max, int found) {
        if(found < min || found > max) {
            LukasNutrients.LOGGER.warn("Compatible config versions: [" + min +", " + max +"]. Found " + found + "!");
            return false;
        }
        return true;
    }
}
