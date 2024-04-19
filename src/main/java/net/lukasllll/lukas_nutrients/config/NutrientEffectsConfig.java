package net.lukasllll.lukas_nutrients.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.DietEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NutrientEffectsConfig {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_effects.json";
    public static NutrientEffectsConfig DATA;

    private ConfigNutrientEffect[] baseEffects;
    private ConfigNutrientEffect[] nutrientEffects;

    public static void create() {
        DATA = getDefaultNutrientEffects();

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
            DATA = gson.fromJson(reader, NutrientEffectsConfig.class);
            LukasNutrients.LOGGER.debug("Successfully read " + FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NutrientEffectsConfig(ConfigNutrientEffect[] baseEffects, ConfigNutrientEffect[] nutrientEffects) {
        this.baseEffects = baseEffects;
        this.nutrientEffects = nutrientEffects;
    }

    public List<DietEffect> getBaseEffects() {
        ArrayList<DietEffect> out = new ArrayList<>();
        for(int i = 0; i < baseEffects.length; i++) {
            out.add(baseEffects[i].getEffect());
        }
        return out;
    }

    public List<DietEffect> getNutrientEffects() {
        ArrayList<DietEffect> out = new ArrayList<>();
        for(int i = 0; i < nutrientEffects.length; i++) {
            out.add(nutrientEffects[i].getEffect());
        }
        return out;
    }

    private static NutrientEffectsConfig getDefaultNutrientEffects() {

        String addition = "ADDITION";
        String multiply_total = "MULTIPLY_TOTAL";


        ArrayList<ConfigNutrientEffect> baseEffectsList = new ArrayList<>();
        baseEffectsList.add(new ConfigNutrientEffect(-1, -1, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -4.0d, addition));

        ArrayList<ConfigNutrientEffect> nutrientEffectsList = new ArrayList<>();
        nutrientEffectsList.add(new ConfigNutrientEffect(0,0, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(0,1, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(0,2, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(0,3, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));

        nutrientEffectsList.add(new ConfigNutrientEffect(7,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), 2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(8,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.ATTACK_DAMAGE).toString(), 1.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(9,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), 2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect(10,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MOVEMENT_SPEED).toString(), 0.1d, multiply_total));

        ConfigNutrientEffect[] baseEffects = baseEffectsList.toArray(ConfigNutrientEffect[]::new);
        ConfigNutrientEffect[] nutrientEffects = nutrientEffectsList.toArray(ConfigNutrientEffect[]::new);

        return new NutrientEffectsConfig(baseEffects, nutrientEffects);
    }

    private static class ConfigNutrientEffect{
       int minScore;
       int maxScore;
       String attribute;
       double amount;
       String operation;

       public ConfigNutrientEffect(int minScore, int maxScore, String attribute, double amount, String operation) {
           this.minScore = minScore;
           this.maxScore = maxScore;
           this.attribute = attribute;
           this.amount = amount;
           this.operation = operation;
       }

       public DietEffect getEffect() {
           return new DietEffect(minScore, maxScore, attribute, amount, operation);
       }
    }
}
