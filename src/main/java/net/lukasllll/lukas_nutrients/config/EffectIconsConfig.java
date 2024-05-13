package net.lukasllll.lukas_nutrients.config;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;

public record EffectIconsConfig(int configVersion, LinkedHashMap<String, String> effectIconMapping) {
    public static final String FILE_PATH = Config.FOLDER_FILE_PATH + "/effect_icons.json";
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int MIN_COMPATIBLE_CONFIG_VERSION = 1;
    private static final int MAX_COMPATIBLE_CONFIG_VERSION = 1;

    private static final String POSITIVE_SUFFIX = ".pos";
    private static final String NEGATIVE_SUFFIX = ".neg";

    public static EffectIconsConfig DATA = null;

    private static boolean validate(EffectIconsConfig config) {
        return Config.checkConfigVersion(MIN_COMPATIBLE_CONFIG_VERSION, MAX_COMPATIBLE_CONFIG_VERSION, config.configVersion());
    }

    public static void read() {
        DATA = Config.readConfigFile(FILE_PATH, EffectIconsConfig.class,EffectIconsConfig::validate, EffectIconsConfig::getDefaultEffectIcons);
    }

    private static EffectIconsConfig getDefaultEffectIcons() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH) + POSITIVE_SUFFIX, "health_boost");
        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH) + NEGATIVE_SUFFIX, "wither");

        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.ATTACK_DAMAGE) + POSITIVE_SUFFIX, "strength");
        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.ATTACK_DAMAGE) + NEGATIVE_SUFFIX, "weakness");

        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.MOVEMENT_SPEED) + POSITIVE_SUFFIX, "speed");
        map.put(ForgeRegistries.ATTRIBUTES.getKey(Attributes.MOVEMENT_SPEED) + NEGATIVE_SUFFIX, "slowness");

        return new EffectIconsConfig(CURRENT_CONFIG_VERSION, map);
    }

    public static String getEffectIcon(String attributeDescriptionId, double modifier) {
        return getEffectIcon(attributeDescriptionId, modifier >= 0);
    }

    public static String getEffectIcon(String attributeDescriptionId, boolean positive) {
        if(DATA == null) return null;
        return DATA.effectIconMapping.getOrDefault(attributeDescriptionId + (positive ? POSITIVE_SUFFIX : NEGATIVE_SUFFIX), "regeneration");
    }
}
