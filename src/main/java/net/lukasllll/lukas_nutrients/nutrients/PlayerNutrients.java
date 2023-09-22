package net.lukasllll.lukas_nutrients.nutrients;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.effects.DietEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

public class PlayerNutrients {

    private NutrientGroup[] groups;
    private double[] amounts;               //how many nutrients the player has of each group
    private double[] exhaustionLevels;      //how much exhaustion each group has. Exhaustion increases until it reaches 4.0. Then resets and nutrients are subtracted
    private int[] ranges;                   //in which of the five segments the amount falls
    private int[] scores;                   //the score of the given range
    private int totalScore;                 //sum of all the scores

    private boolean dirty = true;           //dirty = true, when something has changed and the client needs to be updated

    private double totalFoodLevel = -1;     //used in handleNutrientDecay() to find out whether the player has lost hunger

    public PlayerNutrients() {
        this.groups = NutrientGroup.getFoodGroups();
        this.amounts = new double[this.groups.length];
        this.exhaustionLevels = new double[this.groups.length];
        this.ranges = new int[this.groups.length];
        this.scores = new int[this.groups.length];
    }


    public void recalculateAll() {
        for(int i = 0; i< groups.length; i++) {
            recalculateNutrient(i);
        }
    }

    //recalculates the range and score for the given nutrients and updates the totalScore accordingly
    private void recalculateNutrient(int nutrient) {
        //subtracts the old score from the total. The new score will be added back in at the end.
        totalScore -= scores[nutrient];
        //sets the range to -1 to mark, that it is currently not determined.
        ranges[nutrient] = -1;
        //gets the point ranges (where the different segments start/end from the FoodGroup
        int[] pointRanges = groups[nutrient].getPointRanges();
        //the range is determined by looping through the segment end points. If the nutrient amount is smaller than the end point value,
        //the value falls inside that segment.
        for(int i=0; i<pointRanges.length; i++) {
            if(Math.max(0,amounts[nutrient] -1) < pointRanges[i]) {
                ranges[nutrient] = i;
                break;
            }
        }
        //if the range has not yet been determined, the amount must fall in the last segment
        if(ranges[nutrient] == -1) {
            ranges[nutrient] = 4;
        }
        //scores are given in accordance with the range
        switch(ranges[nutrient]) {
            case 0:
            case 4: scores[nutrient] = 0; break;
            case 1:
            case 3: scores[nutrient] = 1; break;
            case 2: scores[nutrient] = 2; break;
        }
        //the new score is added back in
        totalScore += scores[nutrient];
        //something changed, so the Object is now dirty
        dirty = true;
    }

    //this function handles how nutrients decay over time. It is called every tick
    public void handleNutrientDecay(ServerPlayer player) {
        //first it looks whether the player has lost food and/or saturation
        FoodData foodData = player.getFoodData();
        double oldTotalFoodLevel = totalFoodLevel;
        totalFoodLevel = foodData.getFoodLevel() + (double) foodData.getSaturationLevel();
        //if the current food + saturation is lower than the previous one
        if(totalFoodLevel < oldTotalFoodLevel) {
            double foodLevelDifference = oldTotalFoodLevel - totalFoodLevel;
            //exhaustion increases. If the amount falls into the lowest range 0.08 is added, otherwise 0.16
            for(int i=0; i<groups.length; i++) {
                increaseExhaustion(i, foodLevelDifference * (ranges[i] < 1 ? 0.08 : 0.16));
            }
        }
        //additionally, if any amount falls into the highest two ranges (3 & 4) additional exhaustion is added
        //randomly, though it should average out to about 0.003 * 0.1 * 20 * 60 = 0.36 exhaustion per minute,
        //which means one additional nutrient amount lost about every 11 minutes.
        for(int i=0; i<groups.length; i++) {
            if(ranges[i] > 2 && player.getRandom().nextDouble() < 0.003) {
                increaseExhaustion(i, 0.1);
            }
        }

        //if anything changed the client is then informed and any changes in diet effects are applied
        if(isDirty()) {
            updateClient(player);
            DietEffects.apply(player);
        }
    }

    public void increaseAllExhaustion(double amount) {
        for(int i = 0; i< groups.length; i++) {
            increaseExhaustion(i, amount);
        }
    }

    public void increaseExhaustion(String nutrientID, double amount) {
        increaseExhaustion(getArrayIndex(nutrientID), amount);
    }

    //increases exhaustion and calculates whether nutrients are lost
    public void increaseExhaustion(int nutrient, double amount) {
        //adds the exhaustion
        exhaustionLevels[nutrient] += amount;
        //if the exhaustion reaches 4.0 or more, 4.0 is subtracted and one nutrient amount is lost
        if(exhaustionLevels[nutrient] >= 4.0) {
            exhaustionLevels[nutrient] -= 4.0;
            addAmount(nutrient, -1.0);
        }
        dirty = true;
    }

    //resets all nutrient amounts to their groups default value. Is called after a player died.
    public void setToDefault() {
        for(int i = 0; i< groups.length; i++) {
            setAmount(i, groups[i].getDefaultAmount());
        }
    }

    public void addAmount(int nutrient, double amount){
        setAmount(nutrient, amounts[nutrient] + amount);
    }

    public void setAmount(String nutrientID, double amount) {
        int arrayIndex = this.getArrayIndex(nutrientID);
        if(arrayIndex == -1) return;
        this.setAmount(arrayIndex, amount);
    }

    //sets the amount of a given nutrient and then recalculates
    public void setAmount(int nutrient, double amount) {
        amounts[nutrient] = Math.min(Math.max(0, amount), 24);
        recalculateNutrient(nutrient);
        dirty = true;
    }

    public double[] getNutrientAmounts() {
        return amounts;
    }

    public double[] getExhaustionLevels() {
        return exhaustionLevels;
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
        return this.groups[arrayIndex].getDisplayname();
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void copyFrom(PlayerNutrients source) {
        this.groups = source.groups;
        this.amounts = source.amounts;
        this.exhaustionLevels = source.exhaustionLevels;
    }

    //sends a packet to the client containing all relevant information
    public void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsDataSyncS2CPacket(getNutrientAmounts(), getExhaustionLevels(), getNutrientRanges(), getNutrientScores(), getTotalScore()), player);
        dirty = false;
    }

    //saves nutrient amounts and exhaustion. All other values can be calculated from these two
    public void saveNBTData(CompoundTag nbt) {
        for(int i = 0; i< groups.length; i++) {
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + groups[i].getID(), amounts[i]);
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + groups[i].getID() + "_exhaustion", exhaustionLevels[i]);
        }
    }

    //loads nutrient amounts and exhaustion levels and then recalculates the rest.
    public void loadNBTData(CompoundTag nbt) {
        for(int i = 0; i< groups.length; i++) {
            setAmount(i,nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + groups[i].getID()));
            exhaustionLevels[i] = nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + groups[i].getID() + "_exhaustion");
        }
        recalculateAll();
        dirty = true;
    }

    //gets the array index from a nutrientID. Returns -1 if no such ID exists.
    private int getArrayIndex(String nutrientID) {
        for(int i = 0; i< groups.length; i++) {
            if(groups[i].getID().equals(nutrientID)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isDirty() {
        return dirty;
    }
}
