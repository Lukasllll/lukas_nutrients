package net.lukasllll.lukas_nutrients.util;

import net.lukasllll.lukas_nutrients.nutrients.food.NutrientProperties;

public interface INutrientPropertiesHaver {
    NutrientProperties getFoodNutrientProperties();
    void setFoodNutrientProperties(NutrientProperties properties);
    boolean hasFoodNutrientProperties();
}
