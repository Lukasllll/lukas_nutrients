package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;

public class ClientNutrientData {
    private static NutrientGroup[] Groups= NutrientGroup.getNutrientGroups();
    private static double[] amounts;
    private static double[] exhaustionLevels;
    private static int[] ranges;
    private static int[] scores;
    private static int totalScore;

    public static void set(double[] amounts, double exhaustionLevels[], int[] ranges, int[] scores, int totalScore) {
        ClientNutrientData.amounts = amounts;
        ClientNutrientData.exhaustionLevels = exhaustionLevels;
        ClientNutrientData.ranges = ranges;
        ClientNutrientData.scores = scores;
        ClientNutrientData.totalScore = totalScore;
    }

    public static double[] getPlayerNutrientAmounts() {
        return ClientNutrientData.amounts;
    }

    public static double[] getPlayerExhaustionLevels() {
        return ClientNutrientData.exhaustionLevels;
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

    public static NutrientGroup[] getFoodGroups() {return Groups;}

}
