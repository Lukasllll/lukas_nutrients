package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class EffectIconsConfig {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/effect_icons.json";

    private static final String POSITIVE_SUFFIX = ".pos";
    private static final String NEGATIVE_SUFFIX = ".neg";

    public static EffectIconsConfig DATA = null;
    public HashMap<String, String> effectIconMapping;

    public static void create() {
        DATA = getDefaultEffectIcons();

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
            DATA = gson.fromJson(reader, EffectIconsConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EffectIconsConfig(HashMap<String, String> map) { this.effectIconMapping = map; }

    private static EffectIconsConfig getDefaultEffectIcons() {
        HashMap<String, String> map = new HashMap<>();

        map.put(Attributes.MAX_HEALTH.getDescriptionId() + POSITIVE_SUFFIX, "health_boost");
        map.put(Attributes.MAX_HEALTH.getDescriptionId() + NEGATIVE_SUFFIX, "wither");

        map.put(Attributes.ATTACK_SPEED.getDescriptionId() + POSITIVE_SUFFIX, "haste");
        map.put(Attributes.ATTACK_SPEED.getDescriptionId() + NEGATIVE_SUFFIX, "mining_fatigue");

        map.put(Attributes.MOVEMENT_SPEED.getDescriptionId() + POSITIVE_SUFFIX, "speed");
        map.put(Attributes.MOVEMENT_SPEED.getDescriptionId() + NEGATIVE_SUFFIX, "slowness");

        return new EffectIconsConfig(map);
    }

    public static String getEffectIcon(String attributeDescriptionId, double modifier) {
        if(DATA == null) return null;
        return DATA.effectIconMapping.getOrDefault(attributeDescriptionId + (modifier >= 0 ? POSITIVE_SUFFIX : NEGATIVE_SUFFIX), "regeneration");
    }
}
