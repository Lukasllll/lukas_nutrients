package net.lukasllll.lukas_nutrients.config;

import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;

public record EdibleBlocksConfig (int configVersion, LinkedHashMap<String, Integer> edibleBlocks){
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/edible_blocks.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;

    public static EdibleBlocksConfig DATA = null;

    private static boolean validate(EdibleBlocksConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, EdibleBlocksConfig.class, EdibleBlocksConfig::validate, EdibleBlocksConfig::getDefaultEdibleBlocks);
    }

    private static EdibleBlocksConfig getDefaultEdibleBlocks() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

        //minecraft
        map.put(ForgeRegistries.BLOCKS.getKey(Blocks.CAKE).toString(), 7);

        //farmers delight
        String farmersdelightNamespace = "farmersdelight";

        map.put(farmersdelightNamespace + ":apple_pie", 4);
        map.put(farmersdelightNamespace + ":sweet_berry_cheesecake", 4);
        map.put(farmersdelightNamespace + ":chocolate_pie", 4);

        //autumnity
        String autumnityNamespace = "autumnity";
        map.put(autumnityNamespace + ":pancake", 2);


        return new EdibleBlocksConfig(CURRENT_CONFIG_VERSION, map);
    }
}
