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

    public double[] getNutrientAmounts() {
        return amounts;
    }

    public double getNutrientAmount(int i) {
        return amounts[i];
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
}
