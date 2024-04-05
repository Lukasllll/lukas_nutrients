package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class EdibleBlocksConfig{
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/edible_blocks.json";

    public static EdibleBlocksConfig DATA = null;
    public HashMap<String, Integer> edibleBlocks;

    public static void create() {
        DATA = getDefaultEdibleBlocks();

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
            DATA = gson.fromJson(reader, EdibleBlocksConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public EdibleBlocksConfig(HashMap<String, Integer> edibleBlocks) { this.edibleBlocks = edibleBlocks; }

    private static EdibleBlocksConfig getDefaultEdibleBlocks() {
        HashMap<String, Integer> map = new HashMap<>();

        map.put(Registry.BLOCK.getKey(Blocks.CAKE).toString(), 7);

        return new EdibleBlocksConfig(map);
    }
}
