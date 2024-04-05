package net.lukasllll.lukas_nutrients.nutrients.food;

import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;

public class NutrientProperties {
    //Through the ItemMixin class, every item now has NutrientProperties

    private double[] nutrientAmounts;
    private int servings;
    private boolean isIngredient;
    private boolean placeableEdible;        //whether the item can be placed to create an edible block e.g. cake

    public NutrientProperties(double[] nutrientAmounts, boolean isIngredient) {
        this.nutrientAmounts = nutrientAmounts;
        this.isIngredient = isIngredient;
        this.placeableEdible = false;
        this.servings = 1;
    }

    public NutrientProperties(double[] nutrientAmounts, int servings, boolean isIngredient) {
        this.nutrientAmounts = nutrientAmounts;
        this.isIngredient = isIngredient;
        this.placeableEdible = false;
        this.servings = servings;
    }

    public void setPlaceableEdible(boolean b) { placeableEdible = b; }
    public void setServings(int n) { servings = n; }

    public double[] getNutrientAmounts() {
        return nutrientAmounts;
    }

    public double getNutrientAmount(int arrayIndex) {
        return nutrientAmounts[arrayIndex];
    }

    public int getServings() { return servings; }

    public boolean getPlaceableEdible() { return placeableEdible; }

    public double getNutrientAmount(String nutrientID) {
        int arrayIndex = NutrientGroup.getArrayIndex(nutrientID);
        return getNutrientAmount(arrayIndex);
    }

    public boolean isIngredient() {
        return isIngredient;
    }

}
