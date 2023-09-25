package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;

public class NutrientProperties {

    private double[] nutrientAmounts;
    private boolean isIngredient;

    public NutrientProperties(double[] nutrientAmounts, boolean isIngredient) {
        this.nutrientAmounts = nutrientAmounts;
        this.isIngredient = isIngredient;
    }

    public double[] getNutrientAmounts() {
        return nutrientAmounts;
    }

    public double getNutrientAmount(int arrayIndex) {
        return nutrientAmounts[arrayIndex];
    }

    public double getNutrientAmount(String nutrientID) {
        int arrayIndex = NutrientGroup.getArrayIndex(nutrientID);
        return getNutrientAmount(arrayIndex);
    }

    public boolean isIngredient() {
        return isIngredient;
    }

}
