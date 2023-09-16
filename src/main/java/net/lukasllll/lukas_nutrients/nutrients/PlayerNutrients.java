package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.nbt.CompoundTag;

public class PlayerNutrients {

    private FoodGroup[] Groups;
    private double[] amounts;

    public PlayerNutrients() {
        this.Groups=FoodGroup.getFoodGroups();
        this.amounts=new double[this.Groups.length];
    }

    public void setAmount(String nutrientID, double amount) {
        int arrayIndex = this.getArrayIndex(nutrientID);
        if(arrayIndex == -1) return;
        this.setAmount(arrayIndex, amount);
    }

    public void setAmount(int nutrient, double amount) {
        amounts[nutrient] = amount;
    }

    public double[] getNutrientAmounts() {
        return amounts;
    }

    public double getNutrientAmount(String nutrientID) {
        int arrayIndex = this.getArrayIndex(nutrientID);
        if(arrayIndex == -1) return -1;
        return getNutrientAmount(arrayIndex);
    }

    public double getNutrientAmount(int i) {
        return amounts[i];
    }

    public String getDisplayName(String nutrientID) {
        int arrayIndex = this.getArrayIndex(nutrientID);
        if(arrayIndex == -1) return null;
        return this.Groups[arrayIndex].getDisplayname();
    }

    public int getNutrientAmountInt(int i) {
        return (int) (amounts[i]*2);
    }

    public void copyFrom(PlayerNutrients source) {
        this.Groups = source.Groups;
        this.amounts = source.amounts;
    }

    public void saveNBTData(CompoundTag nbt) {
        for(int i=0; i<Groups.length; i++) {
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + Groups[i].getID(), amounts[i]);
        }
    }

    public void loadNBTData(CompoundTag nbt) {
        for(int i=0; i<Groups.length; i++) {
            amounts[i]=nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + Groups[i].getID());
        }
    }

    private int getArrayIndex(String nutrientID) {
        for(int i=0; i< Groups.length; i++) {
            if(Groups[i].getID().equals(nutrientID)) {
                return i;
            }
        }

        return -1;
    }
}
