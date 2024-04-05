package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.food.FoodNutrientProvider;
import net.minecraftforge.fml.loading.FMLPaths;

public class Config {
    /*
    For now this class mostly exists, because of the NutrientsCommand.reloadConfigs() function. Before, the configs were
    only loaded once in the LukasNutrients.commonSetup() function but since there now are two classes needing to load
    the configs, I decided to outsource it here.

    I know this tiny class is a bit ridiculous, but I don't know where else to put the loadConfigs() function.
    I don't feel it is appropriate for the LukasNutrients class as Config.loadConfigs() is just more intuitive than
    LukasNutrients.loadConfigs(). Although writing that, the second option doesn't feel soo bad. Maybe I'll change this
    later but probably not.

    Ah well... that's OOP I guess...
     */

    public static final String FILE_PATH = FMLPaths.CONFIGDIR.get().toString()+"/"+ LukasNutrients.MOD_ID;

    public static void loadConfigs() {
        BaseNutrientsConfig.read();
        FoodNutrientProvider.addNutrientPropertiesFromConfig();
    }
}
