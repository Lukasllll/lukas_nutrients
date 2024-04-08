package net.lukasllll.lukas_nutrients.client;

import net.lukasllll.lukas_nutrients.config.EffectIconsConfig;
import net.lukasllll.lukas_nutrients.nutrients.NutrientGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class ClientNutrientData {
    private static NutrientGroup[] Groups= NutrientGroup.getNutrientGroups();
    private static double[] amounts;
    private static double[] exhaustionLevels;
    private static int[] ranges;
    private static int[] scores;
    private static int totalScore;
    private static List<Triple<String, AttributeModifier.Operation, Double>> activeEffects;


    public static void set(double[] amounts, double exhaustionLevels[], int[] ranges, int[] scores, int totalScore, List<Triple<String, AttributeModifier.Operation, Double>> activeEffects) {
        ClientNutrientData.amounts = amounts;
        ClientNutrientData.exhaustionLevels = exhaustionLevels;
        ClientNutrientData.ranges = ranges;
        ClientNutrientData.scores = scores;
        ClientNutrientData.totalScore = totalScore;

        ClientNutrientData.activeEffects = activeEffects; /*new ArrayList<>();
        for(Triple<String, AttributeModifier.Operation, Double> effect : activeEffects) {
            ClientNutrientData.activeEffects.add(Triple.of(
                    EffectIconsConfig.getEffectIcon(effect.getLeft(), effect.getRight()),
                    effect.getMiddle(), effect.getRight() ));
        }*/
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

    public static List<Triple<String, AttributeModifier.Operation, Double>> getActiveDietEffects() {
        /*ArrayList<Triple<String, AttributeModifier.Operation, Double>> out = new ArrayList<>();
        out.add(Triple.of("health_boost", AttributeModifier.Operation.ADDITION, 1.0));
        out.add(Triple.of("haste", AttributeModifier.Operation.MULTIPLY_TOTAL, 0.1));
        out.add(Triple.of("speed", AttributeModifier.Operation.MULTIPLY_TOTAL, 0.1));

        return out;*/
        return activeEffects;
    }

}
