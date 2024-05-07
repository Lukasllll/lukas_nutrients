package net.lukasllll.lukas_nutrients.nutrients.player;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.networking.ModMessages;
import net.lukasllll.lukas_nutrients.networking.packet.NutrientsPlayerDataSyncS2CPacket;
import net.lukasllll.lukas_nutrients.nutrients.operators.Constant;
import net.lukasllll.lukas_nutrients.nutrients.operators.ICalcElement;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.NutrientManager;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerNutrients {

    public static final double BASE_DECAY_RATE = 0.16;
    public static final double PASSIVE_DECAY_RATE = 0.1;

    private Nutrient[] nutrients;
    private Operator[] operators;
    private double[] nutrientAmounts;               //how many nutrients the player has of each group
    private double[] exhaustionLevels;      //how much exhaustion each group has. Exhaustion increases until it reaches 4.0. Then resets and nutrients are subtracted
    private int[] ranges;                   //in which of the five segments the amount falls
    private int[] nutrientScores;           //the score of the given range
    private double[] operatorAmounts;
    private int[] operatorScores;

    private boolean dirty = true;           //dirty = true, when something has changed and the client needs to be updated

    private double totalFoodLevel = -1;     //used in handleNutrientDecay() to find out whether the player has lost hunger

    public PlayerNutrients() {
        this.nutrients = NutrientManager.getNutrients();
        this.operators = NutrientManager.getOperators();

        this.nutrientAmounts = new double[this.nutrients.length];
        this.exhaustionLevels = new double[this.nutrients.length];
        this.ranges = new int[this.nutrients.length];
        this.nutrientScores = new int[this.nutrients.length];
        this.operatorAmounts = new double[this.operators.length];
        this.operatorScores = new int[this.operators.length];

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
            previousAmountsAndExhaustion.put(nutrients[i].getID(), Pair.of(nutrientAmounts[i], exhaustionLevels[i]));
        }

        this.nutrients = NutrientManager.getNutrients();
        this.operators = NutrientManager.getOperators();
        this.nutrientAmounts = new double[this.nutrients.length];
        this.exhaustionLevels = new double[this.nutrients.length];
        this.ranges = new int[this.nutrients.length];
        this.nutrientScores = new int[this.nutrients.length];
        this.operatorAmounts = new double[this.operators.length];
        this.operatorScores = new int[this.operators.length];

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
        updateOperators();
    }

    //recalculates the range and score for the given nutrients and updates the totalScore accordingly
    public void recalculateNutrient(int nutrientIndex) {
        //get range
        ranges[nutrientIndex] = nutrients[nutrientIndex].getCurrentRange(nutrientAmounts[nutrientIndex]);

        //scores are given in accordance with the range
        switch (ranges[nutrientIndex]) {
            case 0, 4 -> nutrientScores[nutrientIndex] = 0;
            case 1, 3 -> nutrientScores[nutrientIndex] = 1;
            case 2 -> nutrientScores[nutrientIndex] = 2;
        }
        //something changed, so the Object is now dirty
        dirty = true;
    }

    public void updateOperators() {
        //update the relevant operators:
        for(Operator operator : NutrientManager.getOperators()) {
            int operatorIndex = NutrientManager.getOperatorArrayIndex(operator.getID());

            ArrayList<Double> inputValues = new ArrayList<>();

            ICalcElement[] inputs = operator.getInputs();
            boolean[] takeInputScore = operator.getTakeInputScore();

            for(int i=0; i<inputs.length; i++) {
                if(inputs[i] instanceof Nutrient) {
                    int inputNutrientIndex = NutrientManager.getNutrientArrayIndex(inputs[i].getID());
                    inputValues.add(takeInputScore[i] ? (double) nutrientScores[inputNutrientIndex] : nutrientAmounts[inputNutrientIndex]);
                } else if(inputs[i] instanceof Operator){
                    int inputOperatorIndex = NutrientManager.getOperatorArrayIndex(inputs[i].getID());
                    inputValues.add(takeInputScore[i] ? (double) operatorScores[inputOperatorIndex] : operatorAmounts[inputOperatorIndex]);
                } else if(inputs[i] instanceof Constant) {
                    inputValues.add(((Constant)inputs[i]).getValue());
                }
            }
            operatorAmounts[operatorIndex] = operator.getCurrentAmount(inputValues.iterator());
            operatorScores[operatorIndex] = operator.getCurrentScore(operatorAmounts[operatorIndex]);
        }
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
        setAmount(nutrient, nutrientAmounts[nutrient] + amount);
    }

    public void setAmount(String nutrientID, double amount) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        if(arrayIndex == -1) return;
        this.setAmount(arrayIndex, amount);
    }

    //sets the amount of a given nutrient and then recalculates
    public void setAmount(int nutrientIndex, double amount) {
        nutrientAmounts[nutrientIndex] = Math.min(Math.max(0, amount), 24);
        recalculateNutrient(nutrientIndex);
        updateOperators();
        dirty = true;
    }

    /**
     * Gets either the amount or the score of the operator or nutrient matching the targetID.
     * @param targetID
     * A string consisting of the id of the operator or nutrient and an optional suffix (either ".score" or ".amount").
     * Suffixes are separated from the id by a '.'.
     * @return
     * If given the ".score" suffix, the score will be returned. If given no suffix (or any suffix other than ".score"),
     * the amount will be returned.
     * If there are more than one '.' in the targetID or no corresponding nutrient or operator could be found, -1 is
     * returned.
     */
    public int getValue(String targetID) {
        String[] splitID = targetID.split("\\.");
        if (splitID.length > 2) return -1;
        boolean amount = true;
        if(splitID.length == 2 && splitID[1].equals("score")) amount = false;

        for(int i = 0; i< operators.length; i++) {
            if(operators[i].getID().equals(splitID[0])) return amount ? (int) operatorAmounts[i] : operatorScores[i];
        }
        for(int i=0; i<nutrients.length; i++) {
            if(nutrients[i].getID().equals(splitID[0])) return amount ? (int) nutrientAmounts[i] : nutrientScores[i];
        }

        return -1;
    }

    public double[] getNutrientAmounts() {
        return nutrientAmounts;
    }

    public double[] getExhaustionLevels() {
        return exhaustionLevels;
    }

    public int[] getNutrientScores() {
        return nutrientScores;
    }
    public double[] getOperatorAmounts() {
        return operatorAmounts;
    }

    public int[] getOperatorScores() {
        return operatorScores;
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
        return nutrientAmounts[i];
    }

    public String getDisplayName(String nutrientID) {
        int arrayIndex = NutrientManager.getNutrientArrayIndex(nutrientID);
        if(arrayIndex == -1) return null;
        return this.nutrients[arrayIndex].getDisplayname();
    }

    public void copyFrom(PlayerNutrients source) {
        this.nutrients = source.nutrients;
        this.nutrientAmounts = source.nutrientAmounts;
        this.exhaustionLevels = source.exhaustionLevels;
    }

    //sends a packet to the client containing all relevant information
    public void updateClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new NutrientsPlayerDataSyncS2CPacket(getNutrientAmounts(), getExhaustionLevels(), getNutrientScores(), getOperatorAmounts(), getOperatorScores(), NutrientEffects.getSimplifiedList(player)), player);
        dirty = false;
    }

    //saves nutrient amounts and exhaustion. All other values can be calculated from these two
    public void saveNBTData(CompoundTag nbt) {
        for(int i = 0; i< nutrients.length; i++) {
            nbt.putDouble(LukasNutrients.MOD_ID + "_nutrients_" + nutrients[i].getID(), nutrientAmounts[i]);
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
