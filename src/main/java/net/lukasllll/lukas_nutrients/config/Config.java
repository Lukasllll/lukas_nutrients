package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class Config {
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
}
