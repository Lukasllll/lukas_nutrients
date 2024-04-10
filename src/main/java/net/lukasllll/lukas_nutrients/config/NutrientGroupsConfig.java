package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NutrientGroupsConfig {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_groups.json";

    public static NutrientGroupsConfig DATA;
    public ConfigNutrientGroup[] nutrientGroups;

    public static void create() {
        DATA = getDefaultNutrientGroups();

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
            DATA = gson.fromJson(reader, NutrientGroupsConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public NutrientGroupsConfig(ConfigNutrientGroup[] nutrientGroups) { this.nutrientGroups = nutrientGroups; }

    private static NutrientGroupsConfig getDefaultNutrientGroups() {
        ConfigNutrientGroup[] nutrientGroups = new ConfigNutrientGroup[5];

        nutrientGroups[0] = new ConfigNutrientGroup("fruits", "Fruits", "minecraft:apple", 8, 12, 18, 22, 12);
        nutrientGroups[1] = new ConfigNutrientGroup("grains", "Grains", "minecraft:bread", 8, 10, 18, 20, 10);
        nutrientGroups[2] = new ConfigNutrientGroup("proteins", "Proteins", "minecraft:cooked_beef", 6, 10, 18, 22, 10);
        nutrientGroups[3] = new ConfigNutrientGroup("vegetables", "Vegetables", "minecraft:carrot", 8, 16, 22, 24, 16);
        nutrientGroups[4] = new ConfigNutrientGroup("sugars", "Sugars", "minecraft:honey_bottle", 0, 2, 6, 14, 2);

        return new NutrientGroupsConfig(nutrientGroups);
    }

    public static NutrientGroup[] getNutrientGroups() {
        NutrientGroup[] out = new NutrientGroup[DATA.nutrientGroups.length];
        for(int i=0; i<DATA.nutrientGroups.length; i++) {
            out[i] = DATA.nutrientGroups[i].toNutrientGroup();
        }
        return out;
    }



    private static class ConfigNutrientGroup {
        public String nutrientId;
        public String displayName;
        public String item;
        int[] ranges;
        int defaultAmount;

        public ConfigNutrientGroup(String nutrientId, String displayName, String item, int r1, int r2, int r3, int r4, int defaultAmount) {
            this.nutrientId = nutrientId;
            this.displayName = displayName;
            this.item = item;
            this.ranges = new int[]{r1, r2, r3, r4};
            this.defaultAmount = defaultAmount;
        }

        public NutrientGroup toNutrientGroup() {
            return new NutrientGroup(nutrientId, displayName, item, ranges, defaultAmount);
        }
    }
}
