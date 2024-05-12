package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.operators.Operator;
import net.lukasllll.lukas_nutrients.nutrients.player.effects.NutrientEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;

public class ClientNutrientData {
    private static Nutrient[] nutrients;
    private static Operator[] operators;
    private static HashMap<String, Integer> nutrientArrayIndexMap;
    private static HashMap<String, Integer> operatorArrayIndexMap;
    private static double[] nutrientAmounts;                //how many nutrients the player has of each group
    private static double[] exhaustionLevels;               //how much exhaustion each group has. Exhaustion increases until it reaches 4.0. Then resets and nutrients are subtracted
    private static int[] nutrientRanges;                    //in which of the five segments the amount falls
    private static int[] nutrientScores;                    //the score of the given range
    private static double[] operatorAmounts;
    private static int[] operatorScores;                    //contains the outputs of Operator::getCurrentScore
    private static int[] operatorForcedScores;              //contains the outputs of Operator::getCurrentForcedScore
    private static List<NutrientEffect> activeEffects;
    private static String[] displayOrder;


    public static void setPlayerData(double[] amounts, double[] exhaustionLevels, int[] nutrientScores, double[] operatorAmounts, int[] operatorScores, List<NutrientEffect> activeEffects) {
        ClientNutrientData.nutrientAmounts = amounts;
        ClientNutrientData.exhaustionLevels = exhaustionLevels;
        ClientNutrientData.nutrientScores = nutrientScores;
        ClientNutrientData.operatorAmounts = operatorAmounts;
        ClientNutrientData.operatorScores = operatorScores;

        ClientNutrientData.activeEffects = activeEffects;

        calculateRanges();
        fetchForcedScores();
    }

    public static void setGlobalData(Nutrient[] nutrients, Operator[] operators, String[] displayOrder) {
        ClientNutrientData.nutrients = nutrients;
        ClientNutrientData.operators = operators;
        ClientNutrientData.displayOrder = displayOrder;
        nutrientArrayIndexMap = new HashMap<>();
        for(int i=0; i<nutrients.length; i++) {
            nutrientArrayIndexMap.put(nutrients[i].getID(), i);
        }
        operatorArrayIndexMap = new HashMap<>();
        for(int i=0; i<operators.length; i++) {
            operators[i].fetchInputs(true);
            operatorArrayIndexMap.put(operators[i].getID(), i);
        }

        calculateRanges();
        fetchForcedScores();
    }

    private static void calculateRanges() {
        nutrientRanges = new int[nutrients.length];
        for(int i=0; i< nutrients.length; i++) {
            nutrientRanges[i] = nutrients[i].getCurrentRange(nutrientAmounts[i]);
        }
    }

    private static void fetchForcedScores() {
        operatorForcedScores = new int[operators.length];
        for(int i=0; i<operatorForcedScores.length; i++) {
            operatorForcedScores[i] = operators[i].getCurrentForcedScore(operatorAmounts[i]);
        }
    }

    public static int getNutrientArrayIndex(String id) {
        return nutrientArrayIndexMap.getOrDefault(id, -1);
    }

    public static int getOperatorArrayIndex(String id) {
        return operatorArrayIndexMap.getOrDefault(id, -1);
    }

    public static double[] getNutrientAmounts() {
        return ClientNutrientData.nutrientAmounts;
    }

    public static double[] getPlayerExhaustionLevels() {
        return ClientNutrientData.exhaustionLevels;
    }

    public static int[] getPlayerNutrientScores() {  return ClientNutrientData.nutrientScores; }

    public static int[] getOperatorScores() {
        return ClientNutrientData.operatorScores;
    }

    public static Nutrient getNutrient(String id) {
        int nutrientIndex = nutrientArrayIndexMap.getOrDefault(id, -1);
        if(nutrientIndex == -1) return null;
        return nutrients[nutrientIndex];
    }

    public static int getNutrientScore(String id) {
        int nutrientIndex = nutrientArrayIndexMap.getOrDefault(id, -1);
        if(nutrientIndex == -1) return -1;
        return nutrientScores[nutrientIndex];

    }

    public static double getNutrientAmount(String id) {
        int nutrientIndex = nutrientArrayIndexMap.getOrDefault(id, -1);
        if(nutrientIndex == -1) return -1;
        return nutrientAmounts[nutrientIndex];
    }

    public static int getNutrientRange(String id) {
        int nutrientIndex = nutrientArrayIndexMap.getOrDefault(id, -1);
        if(nutrientIndex == -1) return -1;
        return nutrientRanges[nutrientIndex];
    }

    public static Operator getOperator(String id) {
        int operatorIndex = operatorArrayIndexMap.getOrDefault(id, -1);
        if(operatorIndex == -1) return null;
        return operators[operatorIndex];
    }

    public static double getOperatorAmount(String id) {
        int operatorIndex = operatorArrayIndexMap.getOrDefault(id, -1);
        if(operatorIndex == -1) return -1;
        return operatorAmounts[operatorIndex];
    }

    public static int getOperatorScore(String id) {
        int operatorIndex = operatorArrayIndexMap.getOrDefault(id, -1);
        if(operatorIndex == -1) return -1;
        return operatorScores[operatorIndex];
    }

    public static int getOperatorForcedScore(String id) {
        int operatorIndex = operatorArrayIndexMap.getOrDefault(id, -1);
        if(operatorIndex == -1) return -1;
        return operatorForcedScores[operatorIndex];
    }


    public static Nutrient[] getNutrients() { return nutrients;}

    public static Operator[] getOperators() { return operators;}

    public static String[] getDisplayOrder() { return displayOrder; }

    public static List<NutrientEffect> getActiveDietEffects() {
        return activeEffects;
    }

}
