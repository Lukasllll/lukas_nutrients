package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;

import java.util.ArrayList;

public record NutrientsConfig(int configVersion, ConfigNutrient[] nutrients, ConfigOperator[] operators, String[] displayOrder) {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_groups.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;

    public static NutrientsConfig DATA;

    private static boolean validate(NutrientsConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, NutrientsConfig.class, NutrientsConfig::validate, NutrientsConfig::getDefaultNutrientsConfig);
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

        return new NutrientsConfig(CURRENT_CONFIG_VERSION, nutrients, operators, displayOrder);
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
