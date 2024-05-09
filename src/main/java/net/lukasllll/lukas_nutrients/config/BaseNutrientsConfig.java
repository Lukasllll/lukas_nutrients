package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public record BaseNutrientsConfig (int configVersion, LinkedHashMap<String, List<String>> baseNutrients) {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/base_nutrients.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;

    public static BaseNutrientsConfig DATA = null;

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, BaseNutrientsConfig.class, BaseNutrientsConfig::validate, BaseNutrientsConfig::getDefaultBaseNutrients);
    }

    private static boolean validate(BaseNutrientsConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    private static BaseNutrientsConfig getDefaultBaseNutrients() {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();

        String grains = "grains";       //maybe change? Values already saved in nutrients.NutrientGroup
        String fruits = "fruits";
        String vegetables = "vegetables";
        String proteins = "proteins";
        String sugars = "sugars";
        String none = "none";       //none is no official group, just a placeholder to assign specifically no nutrients to an item

        //minecraft food
        map.put(ForgeRegistries.ITEMS.getKey(Items.APPLE).toString(), buildEntry(0.9, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.CHORUS_FRUIT).toString(), buildEntry( 0.4, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.MELON_SLICE).toString(), buildEntry( 0.9, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.PUMPKIN).toString(), buildEntry( 1.0, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.SWEET_BERRIES).toString(), buildEntry( 1.4, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.GLOW_BERRIES).toString(), buildEntry( 0.9, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.ENCHANTED_GOLDEN_APPLE).toString(), buildEntry( 0.9, fruits));
        map.put(ForgeRegistries.ITEMS.getKey(Items.WARPED_FUNGUS).toString(), buildEntry( 0.5, fruits, vegetables));

        map.put(ForgeRegistries.ITEMS.getKey(Items.BREAD).toString(), buildEntry( 1.0, grains));
        map.put(ForgeRegistries.ITEMS.getKey(Items.WHEAT).toString(), buildEntry( 0.43, grains));

        map.put(ForgeRegistries.ITEMS.getKey(Items.CHICKEN).toString(), buildEntry( 1.6, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.PORKCHOP).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.MUTTON).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.BEEF).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.RABBIT).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.COD).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.SALMON).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.TROPICAL_FISH).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.PUFFERFISH).toString(), buildEntry( 1.5, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.SPIDER_EYE).toString(), buildEntry( 0.9, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.MILK_BUCKET).toString(), buildEntry( 0.1, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.EGG).toString(), buildEntry( 0.5, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.CRIMSON_FUNGUS).toString(), buildEntry( 0.5, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.ROTTEN_FLESH).toString(), buildEntry( 0.5, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.SLIME_BALL).toString(), buildEntry( 0.2, proteins));
        map.put(ForgeRegistries.ITEMS.getKey(Items.BONE).toString(), buildEntry( 0.2, proteins));


        map.put(ForgeRegistries.ITEMS.getKey(Items.CARROT).toString(), buildEntry( 0.9, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.BEETROOT).toString(), buildEntry( 1.8, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.KELP).toString(), buildEntry( 0.1, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.POTATO).toString(), buildEntry( 0.7, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.POISONOUS_POTATO).toString(), buildEntry( 0.4, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.MUSHROOM_STEW).toString(), buildEntry( 1.0, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.RED_MUSHROOM).toString(), buildEntry( 0.54, vegetables));
        map.put(ForgeRegistries.ITEMS.getKey(Items.BROWN_MUSHROOM).toString(), buildEntry( 1.0, vegetables));


        map.put(ForgeRegistries.ITEMS.getKey(Items.HONEY_BOTTLE).toString(), buildEntry( 2.0, sugars));
        map.put(ForgeRegistries.ITEMS.getKey(Items.COCOA_BEANS).toString(), buildEntry( 1.0, sugars));

        map.put(ForgeRegistries.ITEMS.getKey(Items.GLASS_BOTTLE).toString(), buildEntry( 0.0, none));
        map.put(ForgeRegistries.ITEMS.getKey(Items.BOWL).toString(), buildEntry( 0.0, none));

        //farmer's delight
        String farmersdelightNamespace = "farmersdelight";

        map.put(farmersdelightNamespace+":tomato", buildEntry( 0.9, fruits));
        map.put(farmersdelightNamespace+":rotten_tomato", buildEntry( 0.2, fruits));

        map.put(farmersdelightNamespace+":rice", buildEntry( 1.28, grains));

        map.put(farmersdelightNamespace+":ham", buildEntry( 0.9, proteins));

        map.put(farmersdelightNamespace+":cabbage", buildEntry( 1.0, vegetables));
        map.put(farmersdelightNamespace+":onion", buildEntry( 1.8, vegetables));
        map.put(farmersdelightNamespace+":brown_mushroom_colony", buildEntry( 3.0, vegetables));
        map.put(farmersdelightNamespace+":red_mushroom_colony", buildEntry( 1.8, vegetables));

        //nether's delight
        String nethersdelightNamespace = "nethersdelight";

        map.put(nethersdelightNamespace+":propelpearl", buildEntry( 0.9, fruits));

        map.put(nethersdelightNamespace+":crimson_fungus_colony", buildEntry( 1.5, proteins));
        map.put(nethersdelightNamespace+":hoglin_loin", buildEntry( 0.9, proteins));
        map.put(nethersdelightNamespace+":hoglin_ear", buildEntry( 0.6, proteins));
        map.put(nethersdelightNamespace+":strider_slice", buildEntry( 0.9, proteins));

        map.put(nethersdelightNamespace+":warped_fungus_colony", buildEntry( 1.5, vegetables, fruits));

        //autumnity
        String autumnityNamespace = "autumnity";

        map.put(autumnityNamespace+":foul_berries", buildEntry( 0.9, fruits));

        map.put(autumnityNamespace+":turkey", buildEntry( 1.4, proteins));

        map.put(autumnityNamespace+":syrup_bottle", buildEntry( 2.0, sugars));

        //upgrade aquatic
        String upgradeAquaticNamespace = "upgrade_aquatic";

        map.put(upgradeAquaticNamespace+":mulberry", buildEntry( 1.0, fruits));

        map.put(upgradeAquaticNamespace+":pike", buildEntry( 0.9, proteins));
        map.put(upgradeAquaticNamespace+":lionfish", buildEntry( 0.9, proteins));
        map.put(upgradeAquaticNamespace+":perch", buildEntry( 0.9, proteins));

        map.put(upgradeAquaticNamespace+":blue_pickerelweed", buildEntry( 0.1, vegetables));
        map.put(upgradeAquaticNamespace+":purple_pickerelweed", buildEntry( 0.15, vegetables));

        //duckling
        String ducklingNamespace = "duckling";

        map.put(ducklingNamespace+":raw_duck", buildEntry( 1.5, proteins));

        //ecologics
        String ecologicsNamespace = "ecologics";

        map.put(ecologicsNamespace+":walnut", buildEntry( 2.0, grains, vegetables));

        map.put(ecologicsNamespace+":coconut_slice", buildEntry( 0.6, fruits));

        map.put(ecologicsNamespace+":crab_claw", buildEntry( 1.0, proteins));

        //missing wilds
        String missingWildsNamespace = "missingwilds";

        map.put(missingWildsNamespace+":brown_polypore_mushroom", buildEntry( 0.9, vegetables));


        return new BaseNutrientsConfig(CURRENT_CONFIG_VERSION, map);
    }

    public static ArrayList<String> buildEntry(double nutrientEffectiveness, String... nutrientIDs) {
        ArrayList<String> out = new ArrayList<>(List.of(nutrientIDs));
        out.add("" + nutrientEffectiveness);
        return out;
    }

}
