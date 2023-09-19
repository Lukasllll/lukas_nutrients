package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.nutrients.FoodGroup;

public class ClientNutrientData {
    private static FoodGroup[] Groups=FoodGroup.getFoodGroups();
    private static double[] amounts;
    private static int[] ranges;
    private static int[] scores;
    private static int totalScore;

    public static void set(double[] amounts, int[] ranges, int[] scores, int totalScore) {
        ClientNutrientData.amounts = amounts;
        ClientNutrientData.ranges = ranges;
        ClientNutrientData.scores = scores;
        ClientNutrientData.totalScore = totalScore;
    }

    public static double[] getPlayerNutrientAmounts() {
        return ClientNutrientData.amounts;
    }

    public static int[] getPlayerNutrientScores() {
        return ClientNutrientData.scores;
    }

    public static int[] getPlayerNutrientRanges() {
        return ClientNutrientData.ranges;
    }

    public static int getTotalScore() {
        return ClientNutrientData.totalScore;
    }

    public static FoodGroup[] getFoodGroups() {return Groups;}

    private int getArrayIndex(String nutrientID) {
        for(int i=0; i< Groups.length; i++) {
            if(Groups[i].getID().equals(nutrientID)) {
                return i;
            }
        }

        return -1;
    }
}
