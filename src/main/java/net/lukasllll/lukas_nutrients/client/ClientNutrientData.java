package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class ClientNutrientData {
    private static NutrientGroup[] Groups;
    private static double[] amounts;
    private static double[] exhaustionLevels;
    private static int[] ranges;
    private static int[] scores;
    private static int totalScore;
    private static List<Triple<String, AttributeModifier.Operation, Double>> activeEffects;


    public static void set(NutrientGroup[] Groups, double[] amounts, double exhaustionLevels[], int[] ranges, int[] scores, int totalScore, List<Triple<String, AttributeModifier.Operation, Double>> activeEffects) {
        ClientNutrientData.Groups = Groups;
        ClientNutrientData.amounts = amounts;
        ClientNutrientData.exhaustionLevels = exhaustionLevels;
        ClientNutrientData.ranges = ranges;
        ClientNutrientData.scores = scores;
        ClientNutrientData.totalScore = totalScore;

        ClientNutrientData.activeEffects = activeEffects;
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

    public static NutrientGroup[] getNutrientGroups() {return Groups;}

    public static List<Triple<String, AttributeModifier.Operation, Double>> getActiveDietEffects() {
        return activeEffects;
    }

}
