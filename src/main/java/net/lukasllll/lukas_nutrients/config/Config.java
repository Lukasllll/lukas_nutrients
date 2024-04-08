package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Config {
    /*
    This is helper class to help load configs.
     */

    public static final String FOLDER_FILE_PATH = FMLPaths.CONFIGDIR.get().toString()+"/"+ LukasNutrients.MOD_ID;

    public static void loadCommonConfigs() {
        createFolder();
        BaseNutrientsConfig.read();
        EdibleBlocksConfig.read();
        FoodNutrientProvider.addNutrientPropertiesFromConfig();
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
