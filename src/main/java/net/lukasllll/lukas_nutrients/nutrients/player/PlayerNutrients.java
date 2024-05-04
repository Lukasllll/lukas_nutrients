package net.lukasllll.lukas_nutrients.nutrients.player;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsPlayerDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.Sum;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

public class PlayerNutrients {

    public static final double BASE_DECAY_RATE = 0.16;
    public static final double PASSIVE_DECAY_RATE = 0.1;

    private Nutrient[] nutrients;
    private Sum[] sums;
    private double[] amounts;               //how many nutrients the player has of each group
    private double[] exhaustionLevels;      //how much exhaustion each group has. Exhaustion increases until it reaches 4.0. Then resets and nutrients are subtracted
    private int[] ranges;                   //in which of the five segments the amount falls
    private int[] nutrientScores;                   //the score of the given range
    private int[] sumScores;

    private boolean dirty = true;           //dirty = true, when something has changed and the client needs to be updated

    private double totalFoodLevel = -1;     //used in handleNutrientDecay() to find out whether the player has lost hunger

    public PlayerNutrients() {
        this.nutrients = NutrientManager.getNutrients();
        this.sums = NutrientManager.getSums();

        this.amounts = new double[this.nutrients.length];
        this.exhaustionLevels = new double[this.nutrients.length];
        this.ranges = new int[this.nutrients.length];
        this.nutrientScores = new int[this.nutrients.length];
        this.sumScores = new int[this.sums.length];

        this.setToDefault();
    }

    /*
    is called during the /nutrients reload command
    Fetches the NutrientGroups. If there is no change in the amount of groups and their ids, it just recalculatesAll.
    If something changed, it saves all nutrient amounts and exhaustionLevels in a temporary HashMap with the nutrientID as key.
    It then saves the new groups and sets everything to its default value. Then the method goes through the nutrientIDs
    and looks if any of them have been present before the reload. If that's the case, it sets the nutrient amount and
    exhaustion back to what it was before the reload.
     */
    public void reload() {
        HashMap<String, Pair<Double, Double>> previousAmountsAndExhaustion = new HashMap<>();
        for(int i=0; i<nutrients.length; i++) {
            previousAmountsAndExhaustion.put(nutrients[i].getID(), Pair.of(amounts[i], exhaustionLevels[i]));
        }

        this.nutrients = NutrientManager.getNutrients();
        this.sums = NutrientManager.getSums();
        this.amounts = new double[this.nutrients.length];
        this.exhaustionLevels = new double[this.nutrients.length];
        this.ranges = new int[this.nutrients.length];
        this.nutrientScores = new int[this.nutrients.length];
        this.sumScores = new int[this.sums.length];

        this.setToDefault();

        for(int i=0; i<nutrients.length; i++) {
            if(previousAmountsAndExhaustion.containsKey(nutrients[i].getID())) {
                exhaustionLevels[i] = previousAmountsAndExhaustion.get(nutrients[i].getID()).getRight();
                setAmount(i, previousAmountsAndExhaustion.get(nutrients[i].getID()).getLeft());
            }
        }
    }

    public void recalculateAll() {
        for(int i = 0; i< nutrients.length; i++) {
            recalculateNutrient(i);
        }
    }

    //recalculates the range and score for the given nutrients and updates the totalScore accordingly
    public void recalculateNutrient(int nutrientIndex) {
        HashMap<String, List<Integer>> nutrientSumHashMap = NutrientManager.getNutrientSumHashMap();
        //subtracts the old score from all relevant sums. The new score will be added back in at the end.
        for(int sumIndex : nutrientSumHashMap.get(nutrients[nutrientIndex].getID())) {
            sumScores[sumIndex] -= nutrientScores[nutrientIndex];
        }
        //sets the range to -1 to mark, that it is currently not determined.
        ranges[nutrientIndex] = -1;
        //gets the point ranges (where the different segments start/end)
        int[] pointRanges = nutrients[nutrientIndex].getPointRanges();
        //the range is determined by looping through the segment end points. If the nutrient amount is smaller than the end point value,
        //the value falls inside that segment.
        for(int i=0; i<pointRanges.length; i++) {
            if(Math.max(0,amounts[nutrientIndex] -1) < pointRanges[i]) {
                ranges[nutrientIndex] = i;
                break;
            }
        }
        //if the range has not yet been determined, the amount must fall in the last segment
        if(ranges[nutrientIndex] == -1) {
            ranges[nutrientIndex] = 4;
        }
        //scores are given in accordance with the range
        switch(ranges[nutrientIndex]) {
            case 0:
            case 4: nutrientScores[nutrientIndex] = 0; break;
            case 1:
            case 3: nutrientScores[nutrientIndex] = 1; break;
            case 2: nutrientScores[nutrientIndex] = 2; break;
        }
        //the new score is added back in
        for(int sumIndex : nutrientSumHashMap.get(nutrients[nutrientIndex].getID())) {
            sumScores[sumIndex] += nutrientScores[nutrientIndex];
        }
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
            for(int i=0; i<nutrients.length; i++) {
                increaseExhaustion(i, foodLevelDifference * (ranges[i] < 1 ? BASE_DECAY_RATE/2 : BASE_DECAY_RATE));
            }
        }
        //additionally, if any amount falls into the highest two ranges (3 & 4) additional exhaustion is added
        //randomly, though it should average out to about 0.003 * 0.1 * 20 * 60 = 0.36 exhaustion per minute,
        //which means one additional nutrient amount lost about every 11 minutes.
        for(int i=0; i<nutrients.length; i++) {
            if(ranges[i] > 2 && player.getRandom().nextDouble() < 0.003) {
                increaseExhaustion(i, PASSIVE_DECAY_RATE);
            }
        }

        //if anything changed the client is then informed and any changes in diet effects are applied
        if(isDirty()) {
            updateClient(player);
            NutrientEffects.apply(player);
        }
    }

    public void increaseAllExhaustion(double amount) {
        for(int i = 0; i< nutrients.length; i++) {
            increaseExhaustion(i, amount);
        }
    }

    public void increaseExhaustion(String nutrientID, double amount) {
        increaseExhaustion(NutrientManager.getNutrientArrayIndex(nutrientID), amount);
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
        for(int i = 0; i< nutrients.length; i++) {
            setAmount(i, nutrients[i].getDefaultAmount());
        }
    }

    public void addAmounts(double[] amounts, int servings) {
        if(amounts.length != nutrients.length) return;
        for(int i=0; i<nutrients.length; i++) {
            addAmount(i, amounts[i]/ servings);
        }
    }

    public void addAmount(int nutrient, double amount){
        setAmount(nutrient, amounts[nutrient] + amount);
    }

    public void setAmount(String nutrientID, double amount) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        if(arrayIndex == -1) return;
        this.setAmount(arrayIndex, amount);
    }

    //sets the amount of a given nutrient and then recalculates
    public void setAmount(int nutrientIndex, double amount) {
        amounts[nutrientIndex] = Math.min(Math.max(0, amount), 24);
        recalculateNutrient(nutrientIndex);
        dirty = true;
    }

    public int getScore(String targetID) {
        for(int i=0; i<sums.length; i++) {
            if(sums[i].getID().equals(targetID)) return sumScores[i];
        }
        for(int i=0; i<nutrients.length; i++) {
            if(nutrients[i].getID().equals(targetID)) return (int) amounts[i];
        }

        return 0;
    }

    public double[] getNutrientAmounts() {
        return amounts;
    }

    public double[] getExhaustionLevels() {
        return exhaustionLevels;
    }

    public int[] getNutrientScores() {
        return nutrientScores;
    }
    public int[] getSumScores() {
        return sumScores;
    }

    public int[] getNutrientRanges() {
        return ranges;
    }

    public double getNutrientAmount(String nutrientID) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        if(arrayIndex == -1) return -1;
        return getNutrientAmount(arrayIndex);
    }

    public double getNutrientAmount(int i) {
        return amounts[i];
    }

    public String getDisplayName(String nutrientID) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        if(arrayIndex == -1) return null;
        return this.nutrients[arrayIndex].getDisplayname();
    }

    public void copyFrom(PlayerNutrients source) {
        this.nutrients = source.nutrients;
        this.amounts = source.amounts;
        this.exhaustionLevels = source.exhaustionLevels;
    }

    //sends a packet to the client containing all relevant information
    public void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsPlayerDataSyncS2CPacket(getNutrientAmounts(), getExhaustionLevels(), getNutrientScores(), getNutrientRanges(), getSumScores(), NutrientEffects.getSimplifiedList(player)), player);
        dirty = false;
    }

    //saves nutrient amounts and exhaustion. All other values can be calculated from these two
    public void saveNBTData(CompoundTag nbt) {
        for(int i = 0; i< nutrients.length; i++) {
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + nutrients[i].getID(), amounts[i]);
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + nutrients[i].getID() + "_exhaustion", exhaustionLevels[i]);
        }
    }

    //loads nutrient amounts and exhaustion levels and then recalculates the rest.
    public void loadNBTData(CompoundTag nbt) {
        for(int i = 0; i< nutrients.length; i++) {
            setAmount(i,nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + nutrients[i].getID()));
            exhaustionLevels[i] = nbt.getDouble(LukasNutrients.MOD_ID + "_nutrients_" + nutrients[i].getID() + "_exhaustion");
        }
        recalculateAll();
        dirty = true;
    }

    private boolean isDirty() {
        return dirty;
    }
}
