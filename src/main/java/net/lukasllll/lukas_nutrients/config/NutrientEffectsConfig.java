package net.lukasllll.lukas_nutrients.config;

import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public record NutrientEffectsConfig(int configVersion, ConfigNutrientEffect[] baseEffects, ConfigNutrientEffect[] nutrientEffects) {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/nutrient_effects.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;
    public static NutrientEffectsConfig DATA;

    private static boolean validate(NutrientEffectsConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, NutrientEffectsConfig.class, NutrientEffectsConfig::validate, NutrientEffectsConfig::getDefaultNutrientEffects);
    }

    public NutrientEffect[] getBaseEffects() {
        NutrientEffect[] out = new NutrientEffect[baseEffects.length];
        for(int i = 0; i < baseEffects.length; i++) {
            out[i] = baseEffects[i].getEffect();
        }
        return out;
    }

    public NutrientEffect[] getNutrientEffects() {
        NutrientEffect[] out = new NutrientEffect[nutrientEffects.length];
        for(int i = 0; i < nutrientEffects.length; i++) {
            out[i] = nutrientEffects[i].getEffect();
        }
        return out;
    }

    private static NutrientEffectsConfig getDefaultNutrientEffects() {

        String addition = "ADDITION";
        String multiply_total = "MULTIPLY_TOTAL";


        ArrayList<ConfigNutrientEffect> baseEffectsList = new ArrayList<>();
        baseEffectsList.add(new ConfigNutrientEffect("total.amount",-1, -1, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -4.0d, addition));

        ArrayList<ConfigNutrientEffect> nutrientEffectsList = new ArrayList<>();
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",0,0, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",0,1, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",0,2, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",0,3, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), -2.0d, addition));

        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",7,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), 2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",8,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.ATTACK_DAMAGE).toString(), 1.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",9,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH).toString(), 2.0d, addition));
        nutrientEffectsList.add(new ConfigNutrientEffect("total.amount",10,10, ForgeRegistries.ATTRIBUTES.getKey(Attributes.MOVEMENT_SPEED).toString(), 0.1d, multiply_total));

        ConfigNutrientEffect[] baseEffects = baseEffectsList.toArray(ConfigNutrientEffect[]::new);
        ConfigNutrientEffect[] nutrientEffects = nutrientEffectsList.toArray(ConfigNutrientEffect[]::new);

        return new NutrientEffectsConfig(CURRENT_CONFIG_VERSION, baseEffects, nutrientEffects);
    }

    private static class ConfigNutrientEffect{
        String targetID;
        int minScore;
        int maxScore;
        String attribute;
        double amount;
        String operation;

        public ConfigNutrientEffect(String targetID, int minScore, int maxScore, String attribute, double amount, String operation) {
            this.targetID = targetID;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.attribute = attribute;
            this.amount = amount;
            this.operation = operation;
        }

        public NutrientEffect getEffect() {
            return new NutrientEffect(targetID, minScore, maxScore, attribute, amount, operation);
        }
    }
}
