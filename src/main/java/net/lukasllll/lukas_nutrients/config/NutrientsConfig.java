package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.Sum;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NutrientsConfig {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_groups.json";

    public static NutrientsConfig DATA;
    public ConfigNutrient[] nutrients;
    public ConfigSum[] sums;
    public String[] displayOrder;

    public static void create() {
        DATA = getDefaultNutrientsConfig();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try(FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(DATA, writer);
            LukasNutrients.LOGGER.debug("Successfully created " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void read() {
        File path = new File(FILE_PATH);
        if(!path.exists()) {
            LukasNutrients.LOGGER.debug(FILE_PATH + " doesn't exist!");
            create();
            return;
        }

        Gson gson = new Gson();

        try(FileReader reader = new FileReader(path)) {
            DATA = gson.fromJson(reader, NutrientsConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public NutrientsConfig(ConfigNutrient[] nutrients, ConfigSum[] sums, String[] displayOrder) {
        this.nutrients = nutrients;
        this.sums = sums;
        this.displayOrder = displayOrder;
    }

    private static NutrientsConfig getDefaultNutrientsConfig() {
        ConfigNutrient[] nutrients = new ConfigNutrient[5];
        nutrients[0] = new ConfigNutrient("fruits", "Fruits", "minecraft:apple", 8, 12, 18, 22, 12);
        nutrients[1] = new ConfigNutrient("grains", "Grains", "minecraft:bread", 8, 10, 18, 20, 10);
        nutrients[2] = new ConfigNutrient("proteins", "Proteins", "minecraft:cooked_beef", 6, 10, 18, 22, 10);
        nutrients[3] = new ConfigNutrient("vegetables", "Vegetables", "minecraft:carrot", 8, 16, 22, 24, 16);
        nutrients[4] = new ConfigNutrient("sugars", "Sugars", "minecraft:honey_bottle", 0, 2, 6, 14, 2);

        ConfigSum[] sums = new ConfigSum[1];
        sums[0] = new ConfigSum("total", "Diet", 4, 6, "fruits", "grains", "proteins", "vegetables", "sugars");

        String[] displayOrder = new String[]{"fruits", "grains", "proteins", "vegetables", "sugars", NutrientManager.DIVIDER_ID, "total"};

        return new NutrientsConfig(nutrients, sums, displayOrder);
    }

    public static Nutrient[] getNutrients() {
        Nutrient[] out = new Nutrient[DATA.nutrients.length];
        for(int i=0; i<DATA.nutrients.length; i++) {
            out[i] = DATA.nutrients[i].toNutrient();
        }
        return out;
    }

    public static Sum[] getSums() {
        Sum[] out = new Sum[DATA.sums.length];
        for(int i=0; i<DATA.sums.length; i++) {
            out[i] = DATA.sums[i].toSum();
        }
        return out;
    }

    public static String[] getDisplayOrder() {
        return DATA.displayOrder;
    }



    private static class ConfigNutrient {
        public String nutrientId;
        public String displayName;
        public String item;
        int[] ranges;
        int defaultAmount;

        public ConfigNutrient(String nutrientId, String displayName, String item, int r1, int r2, int r3, int r4, int defaultAmount) {
            this.nutrientId = nutrientId;
            this.displayName = displayName;
            this.item = item;
            this.ranges = new int[]{r1, r2, r3, r4};
            this.defaultAmount = defaultAmount;
        }

        public Nutrient toNutrient() {
            return new Nutrient(nutrientId, displayName, item, ranges, defaultAmount);
        }
    }

    private static class ConfigSum {
        public String sumID;
        public String displayName;
        String[] summandIDs;
        int[] ranges;

        public ConfigSum(String sumID, String displayName, int r1, int r2, String... summandIDs) {
            this.sumID = sumID;
            this.displayName = displayName;
            this.ranges = new int[]{r1, r2};
            this.summandIDs = summandIDs;
        }

        public Sum toSum() {
            return new Sum(sumID, displayName, ranges, summandIDs);
        }
    }
}
