package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.minecraft.nbt.CompoundTag;

public class PlayerNutrient {

    private FoodGroup Group;
    private double amount;

    public double getNutrientAmount() {
        return amount;
    }

    public int getNutrientAmountInt() {
        return (int) (amount*2);
    }

    public void addNutrientAmount(double add) {
        this.amount = Math.min(this.amount+add,(double) FoodGroup.MAX_NUTRIENT_AMOUNT);
    }

    public void removeNutrientAmount(double remove) {
        this.amount = Math.max(this.amount-remove,(double) FoodGroup.MIN_NUTRIENT_AMOUNT);
    }

    public void copyFrom(PlayerNutrient source) {
        this.Group = source.Group;
        this.amount = source.amount;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putDouble(LukasNutrients.MOD_ID+"_nutrients_"+Group.getID(),amount);
    }

    public void loadNBTData(CompoundTag nbt) {
        amount = nbt.getDouble(LukasNutrients.MOD_ID+"_nutrients_"+Group.getID());
    }
}
