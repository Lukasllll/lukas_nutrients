package net.lukasllll.lukas_nutrients.integration;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraftforge.fml.ModList;

public class IntegrationHelper {

    private static boolean isFarmersDelightLoaded;
    private static boolean isNethersDelightLoaded;

    public static void init() {
        ModList modList =  ModList.get();
        isFarmersDelightLoaded = modList.isLoaded("farmersdelight");
        LukasNutrients.LOGGER.debug("Farmer's Delight detected: " + IntegrationHelper.isFarmersDelightLoaded());
        isNethersDelightLoaded = modList.isLoaded("nethersdelight");
        LukasNutrients.LOGGER.debug("Nether's Delight detected: " + IntegrationHelper.isNethersDelightLoaded());
    }

    public static boolean isFarmersDelightLoaded() {
        return isFarmersDelightLoaded;
    }
    public static boolean isNethersDelightLoaded() {
        return isNethersDelightLoaded;
    }
}
