package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class NutrientsConfig {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_groups.json";

    public static NutrientsConfig DATA;
    public ConfigNutrient[] nutrients;
    public ConfigOperator[] operators;
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


    public NutrientsConfig(ConfigNutrient[] nutrients, ConfigOperator[] operators, String[] displayOrder) {
        this.nutrients = nutrients;
        this.operators = operators;
        this.displayOrder = displayOrder;
    }

    private static NutrientsConfig getDefaultNutrientsConfig() {
        ConfigNutrient[] nutrients = new ConfigNutrient[5];
        nutrients[0] = new ConfigNutrient("fruits", "Fruits", "minecraft:apple", 8, 12, 18, 22, 12);
        nutrients[1] = new ConfigNutrient("grains", "Grains", "minecraft:bread", 8, 10, 18, 20, 10);
        nutrients[2] = new ConfigNutrient("proteins", "Proteins", "minecraft:cooked_beef", 6, 10, 18, 22, 10);
        nutrients[3] = new ConfigNutrient("vegetables", "Vegetables", "minecraft:carrot", 8, 16, 22, 24, 16);
        nutrients[4] = new ConfigNutrient("sugars", "Sugars", "minecraft:honey_bottle", 0, 2, 6, 14, 2);

        ConfigOperator[] operators = new ConfigOperator[1];
        operators[0] = new ConfigOperator("total", "Diet", "Sum", 4, 6, -1, -1, 5, false, "fruits.score", "grains.score", "proteins.score", "vegetables.score", "sugars.score");

        String[] displayOrder = new String[]{"fruits", "grains", "proteins", "vegetables", "sugars", NutrientManager.DIVIDER_ID, "total"};

        return new NutrientsConfig(nutrients, operators, displayOrder);
    }

    public static Nutrient[] getNutrients() {
        if(DATA.nutrients == null) return new Nutrient[0];
        Nutrient[] out = new Nutrient[DATA.nutrients.length];
        for(int i=0; i<DATA.nutrients.length; i++) {
            out[i] = DATA.nutrients[i].toNutrient();
        }
        return out;
    }

    public static Operator[] getOperators() {
        if(DATA.operators == null) return new Operator[0];
        Operator[] out = new Operator[DATA.operators.length];
        for(int i = 0; i<DATA.operators.length; i++) {
            out[i] = DATA.operators[i].toOperator();
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

    private static class ConfigOperator {
        public String operatorID;
        public String displayName;
        public String operatorType;
        public String[] inputIds;
        public int[] ranges;
        public int basePoint;
        public boolean score;

        public ConfigOperator(String operatorID, String displayName, String operatorType, int r1, int r2, int r3, int r4, int basePoint, boolean score, String... inputIDs) {
            this.operatorID = operatorID;
            this.displayName = displayName;
            this.operatorType = operatorType;
            ArrayList<Integer> tempRanges = new ArrayList<>();
            if(r1 != -1) tempRanges.add(r1);
            if(r2 != -1) tempRanges.add(r2);
            if(r3 != -1) tempRanges.add(r3);
            if(r4 != -1) tempRanges.add(r4);
            this.ranges = tempRanges.stream().mapToInt(Integer::intValue).toArray();
            this.basePoint = basePoint;
            this.score = score;
            this.inputIds = inputIDs;
        }

        public Operator toOperator() {
            return Operator.createOperator(getOperatorTypeFromString(operatorType), operatorID, displayName, ranges, basePoint, score, inputIds);
        }

        public int getOperatorTypeFromString(String s) {
            switch(s) {
                case "Sum" -> { return 0;  }
                case "Product" -> { return 1; }
                case "Min" -> { return 2; }
                case "Max" -> { return 3; }
                case "Invert" -> { return 4; }
                case "Exp" -> { return 5; }
                case "Log" -> { return 6; }
                default -> { return -1; }
            }
        }
    }
}
