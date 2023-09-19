package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsDataSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerNutrients {

    private FoodGroup[] Groups;
    private double[] amounts;
    private int[] ranges;
    private int[] scores;
    private int totalScore;

    public PlayerNutrients() {
        this.Groups = FoodGroup.getFoodGroups();
        this.amounts = new double[this.Groups.length];
        this.ranges = new int[this.Groups.length];
        this.scores = new int[this.Groups.length];
    }

    public void recalculateAll() {
        for(int i=0; i<Groups.length; i++) {
            recalculateNutrient(i);
        }
    }

    private void recalculateNutrient(int nutrient) {
        totalScore -= scores[nutrient];
        scores[nutrient] = -1;
        int[] pointRanges = Groups[nutrient].getPointRanges();
        for(int i=0; i<pointRanges.length; i++) {
            if(Math.max(0,amounts[nutrient] -1) < pointRanges[i]) {
                ranges[nutrient] = i;
                switch(i) {
                    case 0: scores[nutrient] = 0; break;
                    case 1:
                    case 3: scores[nutrient] = 1; break;
                    case 2: scores[nutrient] = 2; break;
                }
                break;
            }
        }
        if(scores[nutrient] == -1) {
            ranges[nutrient] = 4;
            scores[nutrient] = 0;
        }

        totalScore += scores[nutrient];
    }

    public void setToDefault() {
        for(int i=0; i<Groups.length; i++) {
            setAmount(i, Groups[i].getDefaultAmount());
        }
    }

    public void setAmount(String nutrientID, double amount) {
        int arrayIndex = this.getArrayIndex(nutrientID);
        if(arrayIndex == -1) return;
        this.setAmount(arrayIndex, amount);
    }

    public void setAmount(int nutrient, double amount) {
        amounts[nutrient] = amount;
        recalculateNutrient(nutrient);
    }

    public double[] getNutrientAmounts() {
        return amounts;
    }

    public int[] getNutrientScores() {
        return scores;
    }

    public int[] getNutrientRanges() {
        return ranges;
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

    public int getTotalScore() {
        return totalScore;
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

    public void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsDataSyncS2CPacket(getNutrientAmounts(), getNutrientRanges(), getNutrientScores(), getTotalScore()), player);
    }

    public void loadNBTData(CompoundTag nbt) {
        for(int i=0; i<Groups.length; i++) {
            setAmount(i,nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + Groups[i].getID()));
        }
        recalculateAll();
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
