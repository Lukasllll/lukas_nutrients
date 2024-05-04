package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.LukasNutrients;
import net.lukasllll.lukas_nutrients.nutrients.Nutrient;
import net.lukasllll.lukas_nutrients.nutrients.Sum;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;

public class ClientNutrientData {
    private static Nutrient[] nutrients;
    private static Sum[] sums;
    private static HashMap<String, Integer> nutrientArrayIndexMap;
    private static HashMap<String, Integer> sumArrayIndexMap;
    private static double[] amounts;               //how many nutrients the player has of each group
    private static double[] exhaustionLevels;      //how much exhaustion each group has. Exhaustion increases until it reaches 4.0. Then resets and nutrients are subtracted
    private static int[] nutrientScores;                   //the score of the given range
    private static int[] ranges;
    private static int[] sumScores;
    private static List<Triple<String, AttributeModifier.Operation, Double>> activeEffects;
    private static String[] displayOrder;


    public static void setPlayerData(double[] amounts, double[] exhaustionLevels, int[] nutrientScores, int[] ranges, int[] sumScores, List<Triple<String, AttributeModifier.Operation, Double>> activeEffects) {
        ClientNutrientData.amounts = amounts;
        ClientNutrientData.exhaustionLevels = exhaustionLevels;
        ClientNutrientData.nutrientScores = nutrientScores;
        ClientNutrientData.ranges = ranges;
        ClientNutrientData.sumScores = sumScores;

        ClientNutrientData.activeEffects = activeEffects;
    }

    public static void setGlobalData(Nutrient[] nutrients, Sum[] sums, String[] displayOrder) {
        ClientNutrientData.nutrients = nutrients;
        ClientNutrientData.sums = sums;
        ClientNutrientData.displayOrder = displayOrder;
        nutrientArrayIndexMap = new HashMap<>();
        for(int i=0; i<nutrients.length; i++) {
            nutrientArrayIndexMap.put(nutrients[i].getID(), i);
        }
        sumArrayIndexMap = new HashMap<>();
        for(int i=0; i<sums.length; i++) {
            sumArrayIndexMap.put(sums[i].getID(), i);
        }
    }

    public static int getNutrientArrayIndex(String id) {
        return nutrientArrayIndexMap.getOrDefault(id, -1);
    }

    public static int getSumArrayIndex(String id) {
        return sumArrayIndexMap.getOrDefault(id, -1);
    }

    public static double[] getNutrientAmounts() {
        return ClientNutrientData.amounts;
    }

    public static double[] getPlayerExhaustionLevels() {
        return ClientNutrientData.exhaustionLevels;
    }

    public static int[] getPlayerNutrientScores() {  return ClientNutrientData.nutrientScores; }

    public static int[] getSumScores() {
        return ClientNutrientData.sumScores;
    }

    public static Nutrient getNutrient(String id) {
        return nutrients[nutrientArrayIndexMap.get(id)];
    }

    public static int getNutrientScore(String id) {
        return nutrientScores[nutrientArrayIndexMap.get(id)];
    }

    public static int getNutrientRange(String id) {
        return ranges[nutrientArrayIndexMap.get(id)];
    }

    public static double getNutrientAmount(String id) {
        return amounts[nutrientArrayIndexMap.get(id)];
    }

    public static int getSumScore(String id) {
        return sumScores[sumArrayIndexMap.get(id)];
    }


    public static Nutrient[] getNutrients() { return nutrients;}

    public static Sum[] getSums() { return sums;}

    public static String[] getDisplayOrder() { return displayOrder; }

    public static List<Triple<String, AttributeModifier.Operation, Double>> getActiveDietEffects() {
        return activeEffects;
    }

}
